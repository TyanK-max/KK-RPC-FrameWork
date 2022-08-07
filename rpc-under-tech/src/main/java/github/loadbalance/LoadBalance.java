package github.loadbalance;

import github.extension.SPI;
import github.remoting.dto.RpcRequest;

import java.util.List;

/**
 * @author TyanK
 * @description: TODO
 * @date 2022/8/7 14:59
 */
@SPI
public interface LoadBalance {
    /** 
     * @description: Choose one from the list of existing service address list
     * @param serviceList Service address list
     * @param rpcRequest
     * @return target Service address
     */
    String selectServiceAddress(List<String> serviceList, RpcRequest rpcRequest);
}
