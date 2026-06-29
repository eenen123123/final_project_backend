package kr.or.ddit.controller.dashboard;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.or.ddit.finalProject.dto.approval.ApprovalLineDto;
import kr.or.ddit.finalProject.dto.finance.MonthlySalesDto;
import kr.or.ddit.mapper.ApprovalMapper;
import kr.or.ddit.mapper.FinanceMapper;
import kr.or.ddit.mapper.InstructorMonitorMapper;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/dashboard/principal")
@RequiredArgsConstructor
public class DashboardPrincipalController {

    private final ApprovalMapper approvalMapper;
    private final FinanceMapper financeMapper;
    private final InstructorMonitorMapper instructorMonitorMapper;

    @GetMapping
    public String dashboard(Authentication authentication, Model model) {
        String userId = authentication.getName();
        LocalDate now = LocalDate.now();
        String currentYm = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String lastYm = now.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // 결재 대기
        List<ApprovalLineDto> pendingLines = approvalMapper.selectMyPendingLines(userId);
        model.addAttribute("myPendingCnt", pendingLines.size());
        model.addAttribute("pendingLines", pendingLines.size() > 3 ? pendingLines.subList(0, 3) : pendingLines);
        model.addAttribute("allPendingCnt", approvalMapper.countAllPendingLines());

        // 매출 (이번달 + 지난달 delta)
        MonthlySalesDto sales = financeMapper.selectMonthSummary(currentYm);
        MonthlySalesDto lastSales = financeMapper.selectMonthSummary(lastYm);
        long thisTotal = sales != null ? sales.getTotal() : 0L;
        long lastTotal = lastSales != null ? lastSales.getTotal() : 0L;
        int monthDelta = lastTotal > 0 ? (int) Math.round((thisTotal - lastTotal) * 100.0 / lastTotal) : 0;
        model.addAttribute("monthTotal", thisTotal);
        model.addAttribute("monthDelta", monthDelta);

        // 6개월 매출 차트
        List<MonthlySalesDto> yearlySales = financeMapper.selectMonthlySales(String.valueOf(now.getYear()));
        Map<String, Long> salesByMonth = yearlySales.stream()
            .collect(Collectors.toMap(MonthlySalesDto::getMm, MonthlySalesDto::getTotal));
        List<String> chartLabels = new ArrayList<>();
        List<Long> chartData = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate m = now.minusMonths(i);
            chartLabels.add(m.format(DateTimeFormatter.ofPattern("M")) + "월");
            chartData.add(salesByMonth.getOrDefault(m.format(DateTimeFormatter.ofPattern("MM")), 0L));
        }
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartData", chartData);

        // 클래스룸 / 수강생 / 수료예정
        model.addAttribute("activeClassCnt", instructorMonitorMapper.selectActiveClassCount());
        model.addAttribute("totalStudentCnt", instructorMonitorMapper.selectTotalStudentCount());
        model.addAttribute("graduatingCnt", instructorMonitorMapper.countGraduatingStudentsThisMonth());

        return "admin:/dashboard/dashboard-principal";
    }
}
