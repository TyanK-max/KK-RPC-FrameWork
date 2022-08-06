package github.provider;


import github.config.RpcServiceConfig;

/**
 * @description: store Service and provide service obj;
 * @author TyanK
 * @date 2022/8/6 14:20
 */
public interface ServiceProvider {
    
    void addService(RpcServiceConfig rpcServiceConfig);
    
    Object getService(String rpcServiceName);
    
    void publishService(RpcServiceConfig rpcServiceConfig);
}
