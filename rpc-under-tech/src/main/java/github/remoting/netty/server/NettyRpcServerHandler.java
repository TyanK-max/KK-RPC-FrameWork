package github.remoting.netty.server;

import cn.hutool.core.util.StrUtil;
import github.enums.*;
import github.factory.SingletonFactory;
import github.remoting.constants.RpcConstants;
import github.remoting.dto.RpcMessage;
import github.remoting.dto.RpcRequest;
import github.remoting.dto.RpcResponse;
import github.remoting.handler.RequestServiceHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @description: ServeHandler. 处理客户端发来的消息数据
 * @author TyanK
 * @date 2022/8/5 16:32
 */
@Slf4j
public class NettyRpcServerHandler extends SimpleChannelInboundHandler {
    
    private final RequestServiceHandler requestHandler;

    public NettyRpcServerHandler() {
        this.requestHandler = SingletonFactory.getInstance(RequestServiceHandler.class);
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof RpcMessage){
            byte messageType = ((RpcMessage) msg).getMessageType();
            RpcMessage rpcMessage = new RpcMessage();
            rpcMessage.setCodec(SerializationTypeEnum.KRYO.getCode());
            rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
            if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE){
                log.info("client ❤ [{}]",((RpcMessage) msg).getData());
                rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                rpcMessage.setData(RpcConstants.PONG);
            }else{
                log.info("server receive msg: [{}] ",msg);
                RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
                Object result = requestHandler.handle(rpcRequest);
                log.info(StrUtil.format("server get result : {}",result.toString()));
                rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                if(ctx.channel().isActive() && ctx.channel().isWritable()){
                    RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                    rpcMessage.setData(rpcResponse);
                }else {
                    RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                    rpcMessage.setData(rpcResponse);
                    log.error("not writable now ,message dropped!");
                }
            }
            ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        }
    }

    /** 
     * @description: Netty HeartBeat things , IdleStateHandler
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent) evt).state();
            // if the IdleEvent is read_idle ,mean the server don't read anything within 30 sec;
            if (state == IdleState.READER_IDLE){
                log.info("idle check happen, we will close the connection in a while");
                ctx.channel().close();
            }
        }else{
            super.userEventTriggered(ctx,evt);
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
