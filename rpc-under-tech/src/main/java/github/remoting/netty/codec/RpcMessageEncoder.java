package github.remoting.netty.codec;

import github.compress.Compress;
import github.enums.CompressTypeEnum;
import github.enums.SerializationTypeEnum;
import github.extension.ExtensionLoader;
import github.remoting.constants.RpcConstants;
import github.remoting.dto.RpcMessage;
import github.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author TyanK
 * @description: this is the Encoder
 * @date 2022/8/7 16:07
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {

    /** Atomic to make sure in the multi threads environment
     *  requestId is to count how many request has been send ,and it's the RpcRequest Unique identify;
     */
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);
    
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage rpcMessage, ByteBuf out) throws Exception {
        /**
         * Message header eg : [ grpc | 1 (version) | full length |1 (request_type) | 0x01 (kryo) | 0x01 (gzip) | 1 (requestId)]
         *                       4B         1B           4B             1B               1B            1B             4B
         *                       total is 16 Bytes
         *                       next is body
         */
        out.writeBytes(RpcConstants.MAGIC_NUMBER); // 4B "grpc"
        out.writeByte(RpcConstants.VERSION); // 1B
        // leave a place to write the value of full length
        out.writerIndex(out.writerIndex() + 4);
        byte messageType = rpcMessage.getMessageType();
        out.writeByte(messageType); // 1B
        out.writeByte(rpcMessage.getCodec()); // 0x01 ---> 1B  "kryo"
        out.writeByte(CompressTypeEnum.GZIP.getCode()); // 0x01 ---> 1B "gzip"
        out.writeInt(ATOMIC_INTEGER.getAndIncrement()); // requestId 自增 Integer ---> 4B
        // build full length
        byte[] bodyBytes = null;
        int fullLength = RpcConstants.HEAD_LENGTH; // 头部长度为16B
        // if messageType is not heartbeat message,fullLength = head length + body length 排除心跳机制发送的报文
        if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE
                && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
            // serialize the object
            String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
            log.info("codec name: [{}] ", codecName);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                    .getExtension(codecName);
            bodyBytes = serializer.serialize(rpcMessage.getData());
            // compress the bytes
            String compressName = CompressTypeEnum.getName(rpcMessage.getCompress());
            log.info("compress name: [{}]",compressName);
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                    .getExtension(compressName);
            bodyBytes = compress.compress(bodyBytes);
            fullLength += bodyBytes.length;
        }

        if (bodyBytes != null) {
            out.writeBytes(bodyBytes);
        }
        int writeIndex = out.writerIndex(); // 先保存当前指针位置，存好之前预留的空间之后再返回当前位置
        out.writerIndex(writeIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1); // 加 1 是跳过版本号，到之前预留的缓冲区
        out.writeInt(fullLength);
        out.writerIndex(writeIndex); // 恢复指针位置
    }
}
