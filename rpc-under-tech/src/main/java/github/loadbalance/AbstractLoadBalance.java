package github.loadbalance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import github.remoting.dto.RpcRequest;

import java.util.List;

/**
 * @author TyanK
 * @description: TODO
 * @date 2022/8/7 15:03
 */
public abstract class AbstractLoadBalance implements LoadBalance{
    
    @Override
    public String selectServiceAddress(List<String> serviceList, RpcRequest rpcRequest) {
        if(CollUtil.isEmpty(serviceList)){
            return null;
        }
        if(serviceList.size() == 1){
            return serviceList.get(0);
        }
        return doSelect(serviceList,rpcRequest);
    }
    
    public abstract String doSelect(List<String> serviceAddress,RpcRequest rpcRequest);
}
