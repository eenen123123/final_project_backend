package kr.or.ddit.finalProject.service.email;

public interface EmailService {

    /**
     * 6자리 인증 코드를 생성하여 이메일로 전송하는 메서드
     * 
     * @param to 이메일 수신자
     * @return 전송된 6자리 인증 코드
     */
    public String sendEmailSixDigits(String to);

    /**
     * 임의의 본문을 포함한 이메일을 전송하는 메서드
     * 
     * @param to      이메일 수신자
     * @param subject 이메일 제목
     * @param body    이메일 본문
     * @return 발송 결과 메시지
     */
    public String sendEmail(String to, String subject, String body);
}
