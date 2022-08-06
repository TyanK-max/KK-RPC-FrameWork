package github.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author TyanK
 * @description: RpcService 相关的配置属性 例如组名和版本号，以及结合成为一个完整的服务全限定名
 * @date 2022/8/6 14:22
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RpcServiceConfig {

    // service version
    private String version = "";
    // service group
    private String group = "";
    // service body
    private Object service;

    public String getRpcServiceName() {
        return this.getServiceName() + this.getGroup() + this.getVersion();
    }

    private String getServiceName() {
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
