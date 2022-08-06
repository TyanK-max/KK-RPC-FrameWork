package github.remoting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author TyanK
 * @version 1.0
 * @description: 用来组成一个在网络中传输的消息体 is like
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * @date 2022/8/5 16:35
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RpcMessage {
    // rpc message type : request or response
    private byte messageType;
    // serialization type like kryo
    private byte codec;
    // compress type like gzip
    private byte compress;
    private int requestId;
    // request data
    private Object data;
}
