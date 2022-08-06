package github.extension;

import java.lang.annotation.*;

/**
 * @description: with this annotation : 在ExtensionLoader中获取默认实现类或者通过实现类名称来获取实现类
 * @author TyanK
 * @date 2022/8/6 14:41
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPI {
}
