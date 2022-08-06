package github.factory;

import lombok.NoArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author TyanK
 * @version 1.0
 * @description: return an single instance we assign
 * @date 2022/8/5 17:42
 */
@NoArgsConstructor
public final class SingletonFactory {
    private static final Map<String,Object> OBJECT_MAP = new ConcurrentHashMap<>();
    
    public static <T> T getInstance(Class<T> c){
        if(c == null){
            throw new IllegalArgumentException();
        }
        String key = c.toString();
        if(OBJECT_MAP.containsKey(key)){
            return c.cast(OBJECT_MAP.get(key));
        }else {
            return c.cast(OBJECT_MAP.computeIfAbsent(key,k-> {
                try {
                    return c.getDeclaredConstructor().newInstance();
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                    throw new RuntimeException(e.getMessage(),e);
                }
            }));
        }
    }
}
