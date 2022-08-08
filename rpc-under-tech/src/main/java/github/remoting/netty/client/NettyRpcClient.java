package github.remoting.netty.client;

import github.enums.CompressTypeEnum;
import github.enums.SerializationTypeEnum;
import github.extension.ExtensionLoader;
import github.factory.SingletonFactory;
import github.registry.ServiceDiscovery;
import github.remoting.constants.RpcConstants;
import github.remoting.dto.RpcMessage;
import github.remoting.dto.RpcRequest;
import github.remoting.dto.RpcResponse;
import github.remoting.netty.RpcRequestTransport;
import github.remoting.netty.codec.RpcMessageDecoder;
import github.remoting.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author TyanK
 * @description: TODO
 * @date 2022/8/6 20:58
 */
@Slf4j
public final class NettyRpcClient implements RpcRequestTransport {

    private final ServiceDiscovery serviceDiscovery;
    private final ChannelProvider channelProvider;
    private final UnprocessedRequest unprocessedRequest;
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    public NettyRpcClient() {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000) //If the connection is not successful within 5 sec, fail
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        // If no data is sent to the server within 5 seconds, a heartbeat msg will be sent
                        p.addLast(new IdleStateHandler(0,5,0, TimeUnit.SECONDS));
                        p.addLast(new RpcMessageDecoder());
                        p.addLast(new RpcMessageEncoder());
                        p.addLast(new NettyRpcClientHandler());
                    }
                });
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
        this.unprocessedRequest = SingletonFactory.getInstance(UnprocessedRequest.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }


    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        InetSocketAddress serviceAddress = serviceDiscovery.lookupService(rpcRequest);
        Channel channel = getChannel(serviceAddress);
        if(channel.isActive()){
            unprocessedRequest.put(rpcRequest.getRequestId(),resultFuture);
            RpcMessage rpcMessage = RpcMessage.builder().data(rpcRequest)
                    .codec(SerializationTypeEnum.KRYO.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType(RpcConstants.REQUEST_TYPE)
                    .build();
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener)future -> {
                if(future.isSuccess()){
                    log.info("client send msg [{}]",rpcMessage);
                }else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("Send Failed : ",future.cause());
                }
            });
        }else {
            throw new IllegalStateException();
        }
        return resultFuture;
    }
    
    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress){
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener)future -> {
            if (future.isSuccess()){
                log.info("The client has connected [{}] successful!",inetSocketAddress.toString());
                completableFuture.complete(future.channel());
            }else {
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }
    
    public Channel getChannel(InetSocketAddress inetSocketAddress){
        Channel channel = channelProvider.get(inetSocketAddress);
        if(channel == null){
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress,channel);
        }
        return channel;
    }
    
}
