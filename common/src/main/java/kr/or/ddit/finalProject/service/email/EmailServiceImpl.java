package kr.or.ddit.finalProject.service.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import kr.or.ddit.finalProject.dto.email.EmailVerificationDto;
import kr.or.ddit.finalProject.mapper.email.EmailVerificationMapper;
import kr.or.ddit.finalProject.util.RandomSixDigits;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    private final EmailVerificationMapper emailVerificationMapper;

    private final PasswordEncoder passwordEncoder;

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

        EmailVerificationDto emailVerificationDto = new EmailVerificationDto();
        emailVerificationDto.setEmail(to);
        emailVerificationDto.setCode(passwordEncoder.encode(code));

        // 이전에 발송된 코드가 있다면 삭제
        int existingId = emailVerificationMapper.selectIdExistingEmailVerificationId(to).orElse(0);
        log.info("Existing email verification ID for {}: {}", to, existingId);
        if (existingId > 0) {
            emailVerificationMapper.deleteEmailVerificationById(existingId);
        }

        emailVerificationMapper.insertEmailVerification(emailVerificationDto);

        return code;
    }

    @Override
    public boolean checkEmailVerification(String email, String code) {

        EmailVerificationDto emailVerificationDto =
                emailVerificationMapper.selectEmailVerificationDtoByEmail(email);
        if (emailVerificationDto == null) {
            return false; // 이메일에 대한 인증 코드가 없는 경우
        }

        if (!passwordEncoder.matches(code, emailVerificationDto.getCode())) {
            return false; // 코드가 일치하지 않는 경우
        }

        int validId = emailVerificationDto.getId();

        emailVerificationMapper.updateEmailVerifiedAt(validId); // 검증 성공 시 verified_at 업데이트
        return true; // 검증 성공
    }

    @Override
    public String sendEmail(String to, String subject, String body) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendEmail'");
    }

}
