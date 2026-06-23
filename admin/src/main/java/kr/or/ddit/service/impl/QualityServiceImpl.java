package kr.or.ddit.service.impl;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.instructor.InstructorQnaStatsDto;
import kr.or.ddit.finalProject.dto.instructor.OverdueQnaDto;
import kr.or.ddit.mapper.QualityMapper;
import kr.or.ddit.service.QualityService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QualityServiceImpl implements QualityService {

    private final QualityMapper qualityMapper;

    @Override
    public List<InstructorQnaStatsDto> getInstructorQnaStats(String period) {
        LocalDate[] range = toDateRange(period);
        return qualityMapper.selectInstructorQnaStats(range[0], range[1]);
    }

    @Override
    public Map<String, Object> getQnaSummary(String period) {
        LocalDate[] range = toDateRange(period);
        return qualityMapper.selectQnaSummary(range[0], range[1]);
    }

    @Override
    public List<OverdueQnaDto> getOverdueQnaList() {
        return qualityMapper.selectOverdueQnaList();
    }

    // period → [startDate, endDate(null이면 오늘까지)]
    private LocalDate[] toDateRange(String period) {
        LocalDate today = LocalDate.now();
        return switch (period == null ? "30d" : period) {
            case "7d"         -> new LocalDate[]{ today.minusDays(7),  null };
            case "this_month" -> new LocalDate[]{ YearMonth.now().atDay(1), null };
            case "last_month" -> {
                YearMonth last = YearMonth.now().minusMonths(1);
                yield new LocalDate[]{ last.atDay(1), last.atEndOfMonth() };
            }
            default           -> new LocalDate[]{ today.minusDays(30), null }; // 30d
        };
    }
}
