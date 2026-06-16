package kr.or.ddit.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.tuition.BillingSummaryDto;
import kr.or.ddit.finalProject.dto.tuition.TuitionPaymentDto;
import kr.or.ddit.finalProject.dto.tuition.TuitionUnpaidDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.mapper.StaffBillingMapper;
import kr.or.ddit.service.StaffBillingService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StaffBillingServiceImpl implements StaffBillingService {

    private final StaffBillingMapper mapper;

    @Override
    public BillingSummaryDto getSummary(String baseDate) {
        return mapper.selectSummary(baseDate);
    }

    @Override
    public PageResponse<TuitionPaymentDto> searchDailyReceipts(PaginationInfo<Map<String, Object>> paging) {
        return new PageResponse<>(mapper.searchDailyReceipts(paging), mapper.countDailyReceipts(paging));
    }

    @Override
    public PageResponse<TuitionPaymentDto> searchPaymentHistory(PaginationInfo<Map<String, Object>> paging) {
        return new PageResponse<>(mapper.searchPaymentHistory(paging), mapper.countPaymentHistory(paging));
    }

    @Override
    public PageResponse<TuitionUnpaidDto> searchUnpaid(PaginationInfo<Map<String, Object>> paging) {
        return new PageResponse<>(mapper.searchUnpaid(paging), mapper.countUnpaid(paging));
    }
}
