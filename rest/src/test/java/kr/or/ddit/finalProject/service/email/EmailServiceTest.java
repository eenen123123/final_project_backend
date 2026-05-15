package kr.or.ddit.finalProject.service.email;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import kr.or.ddit.finalProject.util.RandomSixDigits;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class EmailServiceTest {
    @Autowired
    private JavaMailSender mailSender;

    @Test
    public void sendEmailTest() {
        String to = "admin@maerchen.dev";
        String subject = "Test Email";
        String body = RandomSixDigits.generate();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@maerchen.dev");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}