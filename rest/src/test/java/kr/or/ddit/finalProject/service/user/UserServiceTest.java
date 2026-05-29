package kr.or.ddit.finalProject.service.user;

import java.time.LocalDate;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.service.member.MemberService;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;

@Slf4j
@SpringBootTest
public class UserServiceTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    MemberService memberService;

    @Test
    void encTest() {
        String rawPassword = "java";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        log.info("원본 비밀번호: {}", rawPassword);
        log.info("인코딩된 비밀번호: {}", encodedPassword);
    }

    @Test
    void verifyTest() {
        String rawPassword = "java";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        log.info("비밀번호 일치 여부: {}", matches);
    }


    @Test
    void insertDummyUser() {
        Faker faker = new Faker(Locale.KOREAN);
        MemberDto memberDto = new MemberDto();
        String userIdPrefix = "dummyUser";
        String password = "java";
        String encodedPassword = passwordEncoder.encode(password);
        int numberOfUsers = 100;

        for (int i = 1; i <= numberOfUsers; i++) {
            String userId = userIdPrefix + i;
            String famName = faker.name().lastName();
            while (famName.length() > 1) {
                famName = faker.name().lastName();
            }
            String firstName = faker.name().firstName();
            String email = userId + faker.internet().emailAddress();
            String birthString = faker.timeAndDate().birthday(10, 40, "yyyy-MM-dd");
            LocalDate birth = LocalDate.parse(birthString);



            log.info("생성된 사용자 정보 - ID: {}, 이름: {}{}, 이메일: {}, 생년월일: {}", userId, famName, firstName,
                    email, birth);


            memberDto.setUserId(userId);

        }



    }
}
