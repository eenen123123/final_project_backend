package kr.or.ddit.finalProject.dto.log;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberLoginLogDto {
    private Long logId;
    private String inputUserId;   // 로그인 화면에서 입력한 ID
    private String userId;        // MEMBER 테이블 실제 ID (미매칭 시 null)
    private String onlineYn;      // Y: 현재 온라인, N: 오프라인
    private String loginSuccessYn; // Y: 성공, N: 실패
    private LocalDateTime loginDt;
    private LocalDateTime logoutDt;
    private String loginIp;
    private String failRsn;
    private LocalDateTime regDate;
    private LocalDateTime modDate;
}
