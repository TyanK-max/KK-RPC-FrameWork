package github.remoting.netty.client;

import github.enums.CompressTypeEnum;
import github.enums.SerializationTypeEnum;
import github.factory.SingletonFactory;
import github.remoting.constants.RpcConstants;
import github.remoting.dto.RpcMessage;
import github.remoting.dto.RpcResponse;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author TyanK
 * @description: Handle the msg from server or send back pong
 * @date 2022/8/6 21:14
 */
@Slf4j
public class NettyRpcClientHandler extends SimpleChannelInboundHandler {
    private final NettyRpcClient nettyRpcClient;
    private final UnprocessedRequest unprocessedRequest;
    private static final AtomicInteger CUR_IDLE_TIMES = new AtomicInteger(0);
    
    public NettyRpcClientHandler() {
        this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
        this.unprocessedRequest = SingletonFactory.getInstance(UnprocessedRequest.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(CUR_IDLE_TIMES.get() == RpcConstants.MAX_IDLE_TIMES.get()){
            log.info("HeartBeat one minute and there aren't any request,so close the channel connection");
            ctx.channel().writeAndFlush("close it").addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            ctx.channel().close();
        }
        if (msg instanceof RpcMessage) {
            RpcMessage tmp = (RpcMessage) msg;
            byte messageType = tmp.getMessageType();
            if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                log.info("server ❤ [{}]", tmp.getData());
            } else if (messageType == RpcConstants.RESPONSE_TYPE) {
                log.info("client receive msg : [{}]", tmp);
                RpcResponse<Object> rpcResponse = (RpcResponse<Object>) tmp.getData();
                unprocessedRequest.complete(rpcResponse);
            }
        }
    }

    /**
     * send heartbeat msg
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                CUR_IDLE_TIMES.getAndIncrement();
                log.info("Write idle happened [{}]", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.KRYO.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client catch exception : ", cause);
        cause.printStackTrace();
        ctx.channel().close();
    }
}
