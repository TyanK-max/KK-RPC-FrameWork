package github.registry.zk;

import cn.hutool.core.collection.CollUtil;
import github.enums.RpcErrorMessageEnum;
import github.exception.RpcException;
import github.extension.ExtensionLoader;
import github.loadbalance.LoadBalance;
import github.registry.ServiceDiscovery;
import github.registry.zk.utils.CuratorUtils;
import github.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author TyanK
 * @description: TODO
 * @date 2022/8/7 14:58
 */
@Slf4j
public class ZkServiceDiscovery implements ServiceDiscovery {
    private final LoadBalance loadBalance;

    public ZkServiceDiscovery() {
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("loadBalance");
    }
    
    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String serviceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, serviceName);
        if(CollUtil.isEmpty(serviceUrlList)){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND,serviceName);
        }
        // load balancing
        log.debug("This is the server ServiceList : ");
        String serviceUrls = CollUtil.join(serviceUrlList," | ");
        log.info(serviceUrls);
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
        log.info("Successfully found the service address:[{}]", targetServiceUrl);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
