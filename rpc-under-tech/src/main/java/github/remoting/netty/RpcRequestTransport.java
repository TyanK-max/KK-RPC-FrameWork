package github.remoting.netty;

import github.extension.SPI;
import github.remoting.dto.RpcRequest;

/**
 * @author TyanK
 * @description: send RpcRequest to server
 * @date 2022/8/6 20:59
 */
@SPI
public interface RpcRequestTransport {
    /**
     * @param rpcRequest send it to server
     * @return get result from server
     */
    Object sendRequest(RpcRequest rpcRequest);
}
