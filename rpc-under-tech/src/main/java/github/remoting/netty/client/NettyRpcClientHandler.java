package github.remoting.netty.client;

import github.enums.CompressTypeEnum;
import github.enums.SerializationTypeEnum;
import github.factory.SingletonFactory;
import github.remoting.constants.RpcConstants;
import github.remoting.dto.RpcMessage;
import github.remoting.dto.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author TyanK
 * @description: Handle the msg from server or send back pong
 * @date 2022/8/6 21:14
 */
@Slf4j
public class NettyRpcClientHandler extends SimpleChannelInboundHandler {
    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler() {
        this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof RpcMessage){
            byte messageType = ((RpcMessage) msg).getMessageType();
            if(messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE){
                log.info("client ❤ ping");
            }else{
                log.info("client receive msg : [{}]",msg);
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent) evt).state();
            if(state == IdleState.WRITER_IDLE){
                log.info("Write idle happen [{}]",ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.KRYO.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
                rpcMessage.setData(RpcConstants.PONG);
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        }else{
            super.userEventTriggered(ctx,evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client catch exception : ",cause);
        cause.printStackTrace();
        ctx.channel().close();
    }
}
