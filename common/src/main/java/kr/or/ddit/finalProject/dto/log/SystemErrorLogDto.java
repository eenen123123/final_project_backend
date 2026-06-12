package kr.or.ddit.finalProject.dto.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemErrorLogDto {
    private Integer errorId;
    private String traceId;
    private String errorCode;
    private String requestUri;
    private String requestIp;
    private String errorMessage;
    private String createdAt;

    // 모니터링 화면 표시용 (조회 시 traceId로 판별) — INSERT 시에는 사용 안 함
    private String traceType;   // MEMBER / ADMIN / ANON
    private String traceUserId; // 식별된 회원ID 또는 관리자ID (익명이면 null)
}
