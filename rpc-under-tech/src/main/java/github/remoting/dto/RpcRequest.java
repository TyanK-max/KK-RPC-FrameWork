package github.remoting.dto;

import lombok.*;

import java.io.Serializable;

/**
 * @author TyanK
 * @version 1.0
 * @description: The request msg transmission we decide;
 * @date 2022/8/5 16:35
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 201909011190603L;
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramsType;
    private String version;
    private String group;

    public String getRpcServiceName() {
        return this.getInterfaceName() + this.getGroup() + this.getVersion();
    }
}
