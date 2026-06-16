package kr.or.ddit.service;

import java.util.Map;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.tuition.BillingSummaryDto;
import kr.or.ddit.finalProject.dto.tuition.TuitionPaymentDto;
import kr.or.ddit.finalProject.dto.tuition.TuitionUnpaidDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

/**
 * 원비 및 수납 관리 서비스
 */
public interface StaffBillingService {

    /** 오늘 수납 현황 카드 요약 */
    BillingSummaryDto getSummary(String baseDate);

    /** 일일 수납 내역 (특정 일자) */
    PageResponse<TuitionPaymentDto> searchDailyReceipts(PaginationInfo<Map<String, Object>> paging);

    /** 수납 이력 조회 (기간/검색) */
    PageResponse<TuitionPaymentDto> searchPaymentHistory(PaginationInfo<Map<String, Object>> paging);

    /** 미납자 관리 (학생별 미납/연체 집계) */
    PageResponse<TuitionUnpaidDto> searchUnpaid(PaginationInfo<Map<String, Object>> paging);
}
