package kr.or.ddit.controller.dashboard;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.or.ddit.finalProject.dto.tuition.BillingSummaryDto;
import kr.or.ddit.finalProject.mapper.MemberMapper;
import kr.or.ddit.finalProject.mapper.StaffMapper;
import kr.or.ddit.mapper.ApprovalMapper;
import kr.or.ddit.mapper.StaffBillingMapper;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/dashboard/staff")
@RequiredArgsConstructor
public class DashboardStaffController {

    private final ApprovalMapper approvalMapper;
    private final StaffBillingMapper staffBillingMapper;
    private final MemberMapper memberMapper;
    private final StaffMapper staffMapper;

    @GetMapping
    public String dashboard(Authentication authentication, Model model) {
        String userId = authentication.getName();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        model.addAttribute("myPendingCnt", approvalMapper.selectMyPendingLines(userId).size());
        model.addAttribute("pendingMemberCnt", memberMapper.countPendingApprovalMembers());

        BillingSummaryDto billing = staffBillingMapper.selectSummary(today);
        model.addAttribute("unpaidCnt", billing != null ? billing.getUnpaidCnt() : 0);
        model.addAttribute("todayAmt", billing != null ? billing.getTodayAmt() : 0L);

        boolean isTeamLead = hasAuthority(authentication, "A001");
        model.addAttribute("isTeamLead", isTeamLead);
        if (isTeamLead) {
            model.addAttribute("teamMemberCnt", staffMapper.countActiveStaffByDept("D100"));
        }

        return "admin:/dashboard/dashboard-staff";
    }

    private boolean hasAuthority(Authentication auth, String authority) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }
}
