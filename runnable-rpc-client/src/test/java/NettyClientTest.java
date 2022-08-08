import github.config.RpcServiceConfig;
import github.proxy.RpcClientProxy;
import github.remoting.netty.RpcRequestTransport;
import github.remoting.netty.client.NettyRpcClient;

public class NettyClientTest {
    public static void main(String[] args) {
        RpcRequestTransport rpcRequestTransport = new NettyRpcClient();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder().group("test1").version("version1").build();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcRequestTransport, rpcServiceConfig);
        PersonService personService = rpcClientProxy.getProxy(PersonService.class);
        Person motherFucker = personService.getPersonalMsg("MotherFucker");
        System.out.println(motherFucker.toString());
    }
}