package kr.or.ddit.finalProject.dto.tuition;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 원비 수납내역 DTO (일일 수납 내역 · 수납 이력 조회 공통)
 * TUITION_PAYMENT + MEMBER(학생명) 조인 결과
 */
@Data
public class TuitionPaymentDto {

    private Long paySn;            // 수납 일련번호 (PK)
    private String userId;        // 학생 ID
    private String studentNm;     // 학생명 (MEMBER.USER_NM 조인)
    private String extPayId;      // 결제ID (외부 경리 프로그램)
    private String payItemCd;     // 항목 코드 (공통코드 222)
    private String payItemNm;     // 항목명 (공통코드 조인)
    private Long payAmt;          // 금액
    private String payMthdCd;     // 결제수단 코드 (공통코드 223)
    private String payMthdNm;     // 결제수단명 (카드/카카오페이/토스페이)
    private String cardNm;        // 카드사명 (OO카드)
    private Integer installmentMm;// 할부개월 (0=일시불)
    private String payStatCd;     // 결제 상태 코드 (공통코드 225)
    private String payStatNm;     // 결제 상태명 (완료/미완료)
    private LocalDateTime payDt;  // 수납일시
}
