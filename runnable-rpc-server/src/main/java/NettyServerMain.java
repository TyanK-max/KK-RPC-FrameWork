import github.config.RpcServiceConfig;
import github.remoting.netty.server.NettyRpcServer;

/**
 * @author TyanK
 * @description: TODO
 * @date 2022/8/7 20:13
 */
public class NettyServerMain {
    public static void main(String[] args) {
        PersonService service = new PersonServiceImpl();
        NettyRpcServer rpcServer = new NettyRpcServer();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .group("test1").version("version1").service(service).build();
        rpcServer.registerService(rpcServiceConfig);
        rpcServer.start();
    }
}
