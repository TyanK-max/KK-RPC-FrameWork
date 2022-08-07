package github.config;

import github.registry.zk.utils.CuratorUtils;
import github.remoting.netty.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * @author TyanK
 * @description: 配置正常关闭事项，例如清除注册数据
 * @date 2022/8/7 17:07
 */
@Slf4j
public class CustomShutdownHook {
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();
    
    public static CustomShutdownHook getCustomShutdownHook(){
        return CUSTOM_SHUTDOWN_HOOK;
    }
    
    public void clearAll(){
        log.info("addShutDownHook to clear the serviceAddress cache");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                /**
                 *  因为笔记本的IPV4 地址容易变化且不确定，就会导致 getLocalHost() 出错，从而不能及时地删除节点，
                 *  导致注册中心有缓存，下次传给客户端的服务ip地址就有误，改进clearRegistry方法即可
                 */
                InetSocketAddress address = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), NettyRpcServer.PORT);
                CuratorUtils.clearRegistry(CuratorUtils.getZkClient(),address);
                log.info("Local IP is " + address.getHostString() + ":" + address.getPort());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }));
    }
}
