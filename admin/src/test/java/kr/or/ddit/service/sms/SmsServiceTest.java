package kr.or.ddit.service.sms;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import kr.or.ddit.finalProject.service.member.ParentService;
import kr.or.ddit.finalProject.service.sms.SmsService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class SmsServiceTest {

    @Autowired
    private SmsService smsService;

    @Autowired
    private ParentService parentService;

    @Test
    void testSendSms() {
        String to = "01094894254";
        String text = "테스트 메시지입니다.";

        // 보낼때만 주석 해제
        // smsService.sendSms(to, text);
    }

    @Test
    void getJoinTokenFromJoinLinkTest() {
        String joinLink =
                "http://localhost:9001/parent/join?token=fcaaecf8-c6de-40c0-ba78-3b35789dbc3b";

        // 토큰 추출
        String token = parentService.getJoinTokenFromJoinLink(joinLink);
        log.info("추출된 토큰: {}", token);
    }
}
