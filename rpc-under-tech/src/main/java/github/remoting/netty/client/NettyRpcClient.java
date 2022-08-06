package github.remoting.netty.client;

import github.extension.ExtensionLoader;
import github.factory.SingletonFactory;
import github.registry.ServiceDiscovery;
import github.remoting.dto.RpcRequest;
import github.remoting.netty.RpcRequestTransport;
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
                        p.addLast(new IdleStateHandler(0,5,0, TimeUnit.SECONDS));
                        
                        
                    }
                });
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }


    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        return null;
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
