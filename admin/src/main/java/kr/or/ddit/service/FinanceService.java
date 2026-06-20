package kr.or.ddit.service;

import java.util.List;
import java.util.Map;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.finance.FinanceTxnDto;
import kr.or.ddit.finalProject.dto.finance.MonthlySalesDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

/**
 * 매출 및 재무 분석 (수입 중심)
 * · 원비 수납 + 온라인 결제 합산
 */
public interface FinanceService {

    /** 요약 카드: 이번 달/전월 매출·증감률·카테고리별 수입 */
    Map<String, Object> getSummary(String ym);

    /** 월별 매출 추이 (12개월 채움) */
    List<MonthlySalesDto> getMonthlySales(String year);

    /** 수입 거래 내역 (서버사이드 페이징) */
    PageResponse<FinanceTxnDto> getTransactions(PaginationInfo<Map<String, Object>> paging);
}
