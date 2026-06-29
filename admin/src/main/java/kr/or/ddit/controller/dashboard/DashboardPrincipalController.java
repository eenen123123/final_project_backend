package kr.or.ddit.controller.dashboard;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
        String currentYm = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        model.addAttribute("myPendingCnt", approvalMapper.selectMyPendingLines(userId).size());
        model.addAttribute("allPendingCnt", approvalMapper.countAllPendingLines());

        MonthlySalesDto sales = financeMapper.selectMonthSummary(currentYm);
        model.addAttribute("monthTotal", sales != null ? sales.getTotal() : 0L);

        model.addAttribute("activeClassCnt", instructorMonitorMapper.selectActiveClassCount());
        model.addAttribute("totalStudentCnt", instructorMonitorMapper.selectTotalStudentCount());
        model.addAttribute("graduatingCnt", instructorMonitorMapper.countGraduatingStudentsThisMonth());

        return "admin:/dashboard/dashboard-principal";
    }
}
