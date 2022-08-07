package github.loadbalance.loadbalancer;

import cn.hutool.core.util.RandomUtil;
import github.loadbalance.AbstractLoadBalance;
import github.remoting.dto.RpcRequest;

import java.util.List;

/**
 * @author TyanK
 * @description: Random LoadBalance
 * @date 2022/8/7 15:10
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    public String doSelect(List<String> serviceAddress, RpcRequest rpcRequest) {
        return serviceAddress.get(RandomUtil.randomInt(serviceAddress.size()));
    }
}
