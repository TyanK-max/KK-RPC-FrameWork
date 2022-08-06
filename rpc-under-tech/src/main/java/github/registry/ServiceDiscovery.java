package github.registry;

import github.extension.SPI;
import github.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * @author TyanK
 * @description: TODO
 * @date 2022/8/6 14:39
 */

@SPI
public interface ServiceDiscovery {
    
    /** 
     * @description： lookup for service from zookeeper
     * @param rpcRequest rpc service pojo
     * @return service address                  
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
