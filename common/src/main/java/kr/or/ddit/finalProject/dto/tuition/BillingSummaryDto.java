package kr.or.ddit.finalProject.dto.tuition;

import lombok.Data;

/**
 * 오늘 수납 현황 카드 요약 DTO
 * 결제 완료(PAY_STAT_CD=01) 기준 당일 집계 + 미납 인원
 */
@Data
public class BillingSummaryDto {

    private Long todayAmt;        // 오늘 수납액 합계
    private Integer todayCnt;     // 오늘 수납 건수
    private Integer unpaidCnt;    // 미납 인원 수
    private Long unpaidTotalAmt;  // 미납 총액
    private Long cardAmt;         // 카드 수납액 (결제수단=01)
    private Integer cardCardCnt;  // 카드 수납 건수
    private Long transferAmt;     // 계좌이체(카카오페이/토스페이=02,03) 수납액
    private Integer transferCnt;  // 계좌이체 건수
}
