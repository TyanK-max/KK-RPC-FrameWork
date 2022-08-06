package github.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author TyanK
 * @version 1.0
 * @description: TODO
 * @date 2022/8/5 16:59
 */
@AllArgsConstructor
@Getter
@ToString
public enum RpcResponseCodeEnum {

    SUCCESS(200, "The remote call is successful"),
    FAIL(500, "The remote call has failed");

    private final int code;
    private final String message;
}
