package kr.or.ddit.finalProject.service.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import kr.or.ddit.finalProject.util.RandomSixDigits;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${email_sender}")
    private String emailSender;

    @Override
    public String sendEmailSixDigits(String to) {
        String code = RandomSixDigits.generate();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailSender);
        message.setTo(to);
        message.setSubject("Your 6-digit verification code");
        message.setText(code);
        mailSender.send(message);
        log.info("Email sent to {} with subject '{}'", to, "Your 6-digit verification code");
        return code;
    }

    @Override
    public String sendEmail(String to, String subject, String body) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendEmail'");
    }

}
