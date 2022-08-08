package github.remoting.netty.codec;

import github.compress.Compress;
import github.enums.CompressTypeEnum;
import github.enums.SerializationTypeEnum;
import github.extension.ExtensionLoader;
import github.remoting.constants.RpcConstants;
import github.remoting.dto.RpcMessage;
import github.remoting.dto.RpcRequest;
import github.remoting.dto.RpcResponse;
import github.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * @author TyanK
 * @description: used this decoder to solve TCP unpacking and sticking problems
 * @date 2022/8/7 16:07
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {
    
    public RpcMessageDecoder(){
        // lengthFieldOffset: magic code is 4B, and version is 1B, and then full length. so value is 5
        // lengthFieldLength: full length is 4B. so value is 4
        // lengthAdjustment: full length include all data and read 9 bytes before, so the left length is (fullLength-9). so values is -9
        // initialBytesToStrip: we will check magic code and version manually, so do not strip any bytes. so values is 0
        this(RpcConstants.MAX_FRAME_LENGTH,5,4,-9,0);
    }
    
    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if(decoded instanceof ByteBuf){
            ByteBuf frame = (ByteBuf) decoded;
            if (frame.readableBytes() >= RpcConstants.TOTAL_LENGTH) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode Frame Error", e);
                } finally {
                    frame.release();
                }
            }
        }
        return decoded;
    }
    
    private Object decodeFrame(ByteBuf in){
        // read ByteBuf in order
        checkMagicNumber(in);
        checkVersion(in);
        // magicNumber and version has been read so begin with full length.
        int fullLength = in.readInt();
        byte messageType = in.readByte();
        byte codecType = in.readByte();
        byte compressType = in.readByte();
        int requestId = in.readInt();
        RpcMessage rpcMessage = RpcMessage.builder()
                .codec(codecType)
                .requestId(requestId)
                .messageType(messageType).build();
        if(messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE){
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        }
        if(messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE){
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        }
        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
        if(bodyLength > 0){
            byte[] bs = new byte[bodyLength];
            in.readBytes(bs);
            String compressName = CompressTypeEnum.getName(compressType);
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(compressName);
            bs = compress.deCompress(bs);
            String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
            log.info("codec name: [{}]",codecName);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(codecName);
            if(messageType == RpcConstants.REQUEST_TYPE){
                RpcRequest rpcRequest = serializer.deSerialize(bs, RpcRequest.class);
                rpcMessage.setData(rpcRequest);
            }else {
                RpcResponse rpcResponse = serializer.deSerialize(bs, RpcResponse.class);
                rpcMessage.setData(rpcResponse);
            }
        }
        return rpcMessage;
    }

    private void checkVersion(ByteBuf in) {
        // read the version and compare
        byte version = in.readByte();
        if (version != RpcConstants.VERSION) {
            throw new RuntimeException("version isn't compatible" + version);
        }
    }

    private void checkMagicNumber(ByteBuf in) {
        // read the first 4 bit, which is the magic number, and compare
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        in.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(tmp));
            }
        }
    }
}
