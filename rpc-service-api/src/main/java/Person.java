import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author TyanK
 * @description: TODO
 * @date 2022/8/7 17:19
 */
@AllArgsConstructor
@Builder
@Data
public class Person {
    private String name;
    private int sex;
    private String wordsToSay;
}
