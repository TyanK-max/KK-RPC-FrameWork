import lombok.extern.slf4j.Slf4j;

/**
 * @author TyanK
 * @description: TODO
 * @date 2022/8/7 17:38
 */
@Slf4j
public class PersonServiceImpl implements PersonService{
    
    static {
        System.out.println("PersonServiceImpl has been created!");
    }
    @Override
    public Person getPersonalMsg(String personName) {
        log.info("PersonService 收到: {}",personName);
        Person person = Person.builder().name(personName).wordsToSay("well, you find me,so i'd like to say hello").sex(1).build();
        log.info("PersonService 返回: {}",person.toString());
        return person;
    }
}
