package github.serialize;

import github.extension.SPI;

/**
 * @author TyanK
 * @description: 序列化接口
 * @date 2022/8/7 15:51
 */
@SPI
public interface Serializer {
    /**
     * @param obj the Object need to be serialized
     * @return return a byte array data;
     */
    byte[] serialize(Object obj);

    /**
     * @param bytes 序列化后的数组
     * @param clazz 需要反序列化成的目标类
     * @param <T>   类的类型 例如 {String.class} 的类型 Class<String>
     * @return 反序列化后的对象
     */
    <T> T deSerialize(byte[] bytes, Class<T> clazz);
}
