package github.registry;

import github.extension.SPI;

import java.net.InetSocketAddress;

/**
 * @author TyanK
 * @description: service registration interface
 * @date 2022/8/6 14:39
 */

@SPI
public interface ServiceRegistry {
    
    /**
     * @description: register service  
     * @param rpcServiceName rpcServiceName
     * @param inetSocketAddress service ip address                      
     * @author TyanK
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
