package kr.or.ddit.finalProject.dto.leave;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 휴가 이력 조회용 DTO.
 *
 * ANNUAL_LEAVE_HISTORY 는 전자결재로 최종 승인된 휴가만 적재되므로,
 * 이 화면(휴가 현황)은 별도 승인상태 없이 "사용 확정된 휴가"만 표시한다.
 * 승인자·진행상태 등은 결재 시스템(APRVL_DOC_SN 으로 연결)에서 관리한다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnualLeaveHistoryDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long annLvSn;        // PK
    private String annUserId;    // MEMBER.USER_ID
    private String annTypeCd;    // 휴가 유형 코드 (공통코드 cl 221)
    private String annStrtYmd;   // 시작일 YYYYMMDD
    private String annEndYmd;    // 종료일 YYYYMMDD
    private BigDecimal annReqDays; // 신청 일수 (반차 고려, 소수 1자리)
    private String annRsnCn;     // 사유
    private Long aprvlDocSn;     // 승인된 전자결재 문서 일련번호 (추적용)
    private LocalDateTime regDt; // 적재 시점

    // ── JOIN 표시용 ──
    private String userName;     // 직원명
    private String deptNm;       // 부서명
    private String jbgrNm;       // 직급명
    private String annTypeNm;    // 휴가 유형명 (COM_CD)
}
