package github.remoting.handler;

import github.exception.RpcException;
import github.factory.SingletonFactory;
import github.provider.ServiceProvider;
import github.provider.ZkServiceProvider;
import github.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author TyanK
 * @version 1.0
 * @description: Handle the service in RpcRequest
 * @date 2022/8/5 17:40
 */
@Slf4j
public class RequestServiceHandler {
    private final ServiceProvider serviceProvider;

    public RequestServiceHandler(ServiceProvider serviceProvider) {
        this.serviceProvider = SingletonFactory.getInstance(ZkServiceProvider.class);
    }
    
    /**
     * @param rpcRequest running the remote call ,return the method
     */
    public Object handle(RpcRequest rpcRequest){
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        return invokeTargetMethod(rpcRequest,service);
    }
    
    /**
     * @param rpcRequest client request
     * @param service service obj
     * @return the result of the target method execution
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest,Object service){
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsType());
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("service:[{}] successful invoke method:[{}]",rpcRequest.getInterfaceName(),rpcRequest.getMethodName());
        } catch (NoSuchMethodException  | InvocationTargetException | IllegalAccessException e) {
            throw new RpcException(e.getMessage(),e);
        }
        return result;
    }
}
