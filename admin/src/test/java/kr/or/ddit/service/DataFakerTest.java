package kr.or.ddit.service;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;

@Slf4j
@SpringBootTest
public class DataFakerTest {
    Faker faker = new Faker(Locale.KOREAN);

    @Test
    void test1() {
        for (int i = 0; i < 100; i++) {
            String familyName = faker.name().lastName();
            
            String name = faker.name().firstName();
            if(name.length() == 1 || familyName.length() >= 2) {
                i--;
                continue;
            }
            log.info("name : {}{}",familyName,name );

        }
    }

    @Test
    void test2() {
        for (int i = 0; i < 100; i++) {
            String state = faker.address().state();
            String city = faker.address().city();
            String street = faker.address().streetName();

            log.info("{} {} {} {} ", state, city, street);
            
        }

        
        
    }
}
