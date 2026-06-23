package kr.or.ddit.service.impl;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.finance.FinanceTxnDto;
import kr.or.ddit.finalProject.dto.finance.MonthlySalesDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.mapper.FinanceMapper;
import kr.or.ddit.service.FinanceService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinanceServiceImpl implements FinanceService {

    private final FinanceMapper mapper;

    @Override
    public Map<String, Object> getSummary(String ym) {
        MonthlySalesDto thisMonth = orZero(mapper.selectMonthSummary(ym));
        MonthlySalesDto prevMonth = orZero(mapper.selectMonthSummary(YearMonth.parse(ym).minusMonths(1).toString()));

        Double growthRate = (prevMonth.getTotal() == 0)
                ? null
                : (thisMonth.getTotal() - prevMonth.getTotal()) * 100.0 / prevMonth.getTotal();

        Map<String, Object> result = new HashMap<>();
        result.put("sales", thisMonth);
        result.put("prevTotal", prevMonth.getTotal());
        result.put("growthRate", growthRate);
        return result;
    }

    @Override
    public List<MonthlySalesDto> getMonthlySales(String year) {
        Map<String, MonthlySalesDto> byMonth = new HashMap<>();
        for (MonthlySalesDto row : mapper.selectMonthlySales(year)) {
            byMonth.put(row.getMm(), row);
        }
        // 데이터 없는 달도 0으로 채워 12개월 정렬 반환
        List<MonthlySalesDto> result = new ArrayList<>(12);
        for (int m = 1; m <= 12; m++) {
            String mm = String.format("%02d", m);
            MonthlySalesDto row = byMonth.get(mm);
            if (row == null) {
                row = new MonthlySalesDto();
                row.setMm(mm);
            }
            result.add(row);
        }
        return result;
    }

    @Override
    public PageResponse<FinanceTxnDto> getTransactions(PaginationInfo<Map<String, Object>> paging) {
        return new PageResponse<>(mapper.selectTransactions(paging), mapper.countTransactions(paging));
    }

    private MonthlySalesDto orZero(MonthlySalesDto dto) {
        return dto != null ? dto : new MonthlySalesDto();
    }
}
