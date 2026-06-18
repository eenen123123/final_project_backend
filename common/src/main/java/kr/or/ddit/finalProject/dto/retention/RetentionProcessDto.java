package kr.or.ddit.finalProject.dto.retention;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 퇴원 방어 상담 프로세스 DTO (목록 집계 / 저장 공용)
 * RETENTION_PROCESS + MEMBER + COM_CD(사유 203 / 결과 227) 조인
 */
@Data
public class RetentionProcessDto {

    private Long rtnpSn;            // 프로세스 일련번호 (PK)
    private String stdUserId;      // 학생 ID
    private String studentNm;      // 학생명
    private String className;      // 담당 반/강좌명
    private String wdrwRsnCd;      // 퇴원 사유 코드 (공통코드 203)
    private String wdrwRsnNm;      // 퇴원 사유명
    private String rtnpCn;         // 상담 내용
    private String rtnpRsltCd;     // 결과 코드 (공통코드 227)
    private String rtnpRsltNm;     // 결과명 (진행중/유지/퇴원)
    private String chrgUserId;     // 담당자 ID
    private String chrgNm;         // 담당자명
    private LocalDateTime rtnpDt;  // 상담 일시

    // ── 목록 집계용 (학생별) ──
    private int cnslCnt;           // 누적 상담 횟수
    private LocalDateTime lastDt;  // 최근 상담일

    // ── 저장 시 사용 ──
    private String rgtrId;         // 등록자 ID
}
