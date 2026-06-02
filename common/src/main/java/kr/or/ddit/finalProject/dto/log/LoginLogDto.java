package kr.or.ddit.finalProject.dto.log;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 로그인 로그 DTO
 */
@Data
public class LoginLogDto {

    private Long logId; // 로그인 로그 번호
    private String userId; // 사용자 아이디
    private String loginIp; // 로그인 IP 주소
    private LocalDateTime loginDt; // 로그인 일시
    private LocalDateTime logoutDt; // 로그아웃 일시
    private String sessionId; // 세션 ID
}
