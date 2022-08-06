package github.extension;

/**
 * @author TyanK
 * @description: Service Cached 缓存层
 * @date 2022/8/6 14:57
 */

public class Holder<T> {
    
    private volatile T value;
    
    public T get(){return value;}
    
    public void set(T value){this.value = value;}
}
