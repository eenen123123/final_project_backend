package kr.or.ddit.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.finance.FinanceTxnDto;
import kr.or.ddit.finalProject.dto.finance.MonthlySalesDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

/**
 * 매출(수입) 집계 Mapper
 * · 원비 수납(TUITION_PAYMENT 완료) + 온라인 결제(ORDERS PAID · ORDER_ITEM) 합산
 */
@Mapper
public interface FinanceMapper {

    /** 특정 월('YYYY-MM') 카테고리별 매출 요약 */
    MonthlySalesDto selectMonthSummary(@Param("ym") String ym);

    /** 연도별(12개월) 카테고리별 매출 */
    List<MonthlySalesDto> selectMonthlySales(@Param("year") String year);

    /** 수입 거래 내역 (서버사이드 페이징) */
    List<FinanceTxnDto> selectTransactions(PaginationInfo<Map<String, Object>> paging);
    int countTransactions(PaginationInfo<Map<String, Object>> paging);
}
