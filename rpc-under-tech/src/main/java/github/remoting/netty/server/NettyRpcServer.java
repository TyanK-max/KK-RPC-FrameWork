package github.remoting.netty.server;

import cn.hutool.core.util.RuntimeUtil;
import github.config.CustomShutdownHook;
import github.config.RpcServiceConfig;
import github.factory.SingletonFactory;
import github.provider.ServiceProvider;
import github.provider.ZkServiceProvider;
import github.remoting.netty.codec.RpcMessageDecoder;
import github.remoting.netty.codec.RpcMessageEncoder;
import github.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;


/**
 * @description: The server. 根据客户端需要的方法调用并且返回处理结果。
 * @author TyanK
 * @date 2022/8/5 16:28
 * @version 1.0
 */
@Slf4j
public class NettyRpcServer {
    public static final int PORT = 8848;
    
    private ServiceProvider serviceProvider = SingletonFactory.getInstance(ZkServiceProvider.class);
    public void registerService(RpcServiceConfig rpcServiceConfig){
        serviceProvider.publishService(rpcServiceConfig);
    }
    
    @SneakyThrows
    public void start(){
        CustomShutdownHook.getCustomShutdownHook().clearAll();
        String host = InetAddress.getLocalHost().getHostAddress();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
                Runtime.getRuntime().availableProcessors() * 2,
                ThreadPoolFactoryUtil.createThreadFactory("service-handler-group",false)
        );
        try {
            bootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // TCP Nagle 该算法的作用是尽可能的发送大数据快
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    // 开启TCP长连接 + 心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    // 用于临时存放三次握手的请求队列最大长度
                    .option(ChannelOption.SO_BACKLOG,128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            // IdleStateHandler 空闲状态处理器 30 秒之内没有读取到数据就关闭连接
                            p.addLast(new IdleStateHandler(30,0,0, TimeUnit.SECONDS));
                            p.addLast(new RpcMessageDecoder());
                            p.addLast(new RpcMessageEncoder());
                            p.addLast(serviceHandlerGroup,new NettyRpcServerHandler());
                        }
                    });
            // 绑定端口，同步等待绑定成功
            ChannelFuture f = bootstrap.bind(host, PORT).sync();
            // 等待服务器监听端口关闭
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("occur github.exception when start server:", e);
        } finally {
            log.error("shutdown bossGroup and workerGroup");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
