package kr.or.ddit.finalProject.dto.finance;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 수입 거래 내역 (원비 수납 + 온라인 결제 통합 ledger)
 */
@Data
public class FinanceTxnDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDateTime txDt; // 거래일시
    private String source; // 구분 (원비수납 / 온라인결제)
    private String category; // 항목 (원비/교재비/입회비/기타/온라인 판매)
    private String purchaser; // 구매자 (학생/회원명)
    private String payMethod; // 결제수단
    private long amount; // 금액
}
