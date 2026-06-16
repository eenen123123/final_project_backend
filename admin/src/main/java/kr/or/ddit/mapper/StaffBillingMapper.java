package kr.or.ddit.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.tuition.BillingSummaryDto;
import kr.or.ddit.finalProject.dto.tuition.TuitionPaymentDto;
import kr.or.ddit.finalProject.dto.tuition.TuitionUnpaidDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

/**
 * 원비 및 수납 관리 매퍼
 * - 일일 수납 내역 / 수납 이력 조회 (TUITION_PAYMENT)
 * - 미납자 관리 (TUITION_BILL 집계)
 * - 오늘 수납 현황 요약
 */
@Mapper
public interface StaffBillingMapper {

    // ── 오늘 수납 현황 카드 요약 ──────────────────────────────
    BillingSummaryDto selectSummary(@Param("baseDate") String baseDate);

    // ── 일일 수납 내역 (특정 일자) ────────────────────────────
    List<TuitionPaymentDto> searchDailyReceipts(PaginationInfo<Map<String, Object>> paging);
    int countDailyReceipts(PaginationInfo<Map<String, Object>> paging);

    // ── 수납 이력 조회 (기간/검색) ────────────────────────────
    List<TuitionPaymentDto> searchPaymentHistory(PaginationInfo<Map<String, Object>> paging);
    int countPaymentHistory(PaginationInfo<Map<String, Object>> paging);

    // ── 미납자 관리 (학생별 미납/연체 집계) ───────────────────
    List<TuitionUnpaidDto> searchUnpaid(PaginationInfo<Map<String, Object>> paging);
    int countUnpaid(PaginationInfo<Map<String, Object>> paging);

    // ── Excel 업로드 적재 ─────────────────────────────────────
    /** 결제ID 중복 여부 (멱등 업로드) */
    int existsByExtPayId(@Param("extPayId") String extPayId);
    /** 수납내역 1건 적재 */
    void insertPayment(TuitionPaymentDto payment);
    /** 방금 적재한 결제ID의 PAY_SN 조회 */
    Long selectPaySnByExtPayId(@Param("extPayId") String extPayId);
    /** 같은 학생·금액의 가장 오래된 미납/연체 청구를 완료(03)로 정산하고 PAY_SN 연결 (0 또는 1건) */
    int settleMatchingBill(@Param("userId") String userId,
                           @Param("paySn") Long paySn,
                           @Param("amount") Long amount);
}
