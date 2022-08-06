package github.remoting.dto;

import github.enums.RpcResponseCodeEnum;
import lombok.*;

import java.io.Serializable;

/**
 * @author TyanK
 * @version 1.0
 * @description: The response msg transmission we decide;
 * @date 2022/8/5 16:35
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RpcResponse<T> implements Serializable {
    private static final long serialVersionUID = 201909011190603L;
    private String requestId;
    // response code
    private Integer code;
    // response msg
    private String message;
    // response body
    private T data;
    
    public static <T> RpcResponse<T> success(T data,String requestId){
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
        response.setMessage(RpcResponseCodeEnum.SUCCESS.getMessage());
        response.setRequestId(requestId);
        if(data != null){
            response.setData(data);
        }
        return response;
    }
    
    public static <T> RpcResponse<T> fail(RpcResponseCodeEnum rpcResponseCodeEnum){
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseCodeEnum.FAIL.getCode());
        response.setMessage(RpcResponseCodeEnum.FAIL.getMessage());
        return response;
    }
    
}
