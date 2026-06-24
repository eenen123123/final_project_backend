package kr.or.ddit.finalProject.dto.certificate;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 증명서 발급 (직원 셀프서비스)
 * · 발급 레코드 + 출력 시 자동 조인되는 직원 정보(스냅샷 저장 안 함)
 */
@Data
public class CertificateIssueDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long certSn; // 발급 일련번호 (PK)
    private String userId; // 신청·발급 대상 직원 (본인)
    private String certTyCd; // 증명서 종류 코드 (공통코드 228)
    private String issueRsn; // 필요 사유
    private String issuePurps; // 제출처(사용처)
    private String prnYn; // 출력 여부 (Y/N) · 1회 출력 제한
    private LocalDateTime issueDt; // 발급일시
    private LocalDateTime prnDt; // 출력일시

    // ── 조회 시 조인 ──────────────────────────────
    private String certTyNm; // 증명서 종류명 (COM_CD)
    private String userName; // 직원 성명 (MEMBER.USER_NAME)
    private LocalDate userBrdt; // 생년월일 (MEMBER.USER_BRDT)
    private String userGndrCd; // 성별 (M/F/U) · 주민번호 마스킹용
    private String userTelno; // 연락처 (MEMBER.USER_TELNO)
    private String userAddr; // 기본 주소 (MEMBER.USER_ADDR)
    private String userDaddr; // 상세 주소 (MEMBER.USER_DADDR)
    private String deptNm; // 부서명(소속)
    private String jbgrNm; // 직급명(직위)
    private LocalDate joinYmd; // 입사일
    private String emplStatNm; // 재직 상태명 (재직/휴직/퇴사)
}
