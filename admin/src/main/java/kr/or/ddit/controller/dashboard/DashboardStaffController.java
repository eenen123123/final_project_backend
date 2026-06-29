package kr.or.ddit.controller.dashboard;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.or.ddit.finalProject.dto.consultation.ConsultationSummaryDto;
import kr.or.ddit.finalProject.dto.retention.RetentionSummaryDto;
import kr.or.ddit.finalProject.dto.tuition.BillingSummaryDto;
import kr.or.ddit.finalProject.mapper.StaffMapper;
import kr.or.ddit.finalProject.mapper.board.QnaMapper;
import kr.or.ddit.mapper.ApprovalMapper;
import kr.or.ddit.mapper.ConsultationMapper;
import kr.or.ddit.mapper.RetentionMapper;
import kr.or.ddit.mapper.StaffBillingMapper;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/dashboard/staff")
@RequiredArgsConstructor
public class DashboardStaffController {

    private final ApprovalMapper approvalMapper;
    private final StaffBillingMapper staffBillingMapper;
    private final StaffMapper staffMapper;
    private final QnaMapper qnaMapper;
    private final ConsultationMapper consultationMapper;
    private final RetentionMapper retentionMapper;

    @GetMapping
    public String dashboard(Authentication authentication, Model model) {
        String userId = authentication.getName();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        model.addAttribute("myPendingCnt", approvalMapper.selectMyPendingLines(userId).size());
        model.addAttribute("unansweredQnaCnt", qnaMapper.countUnansweredQna());
        model.addAttribute("recentQnaList", qnaMapper.selectRecentUnansweredQna(3));

        BillingSummaryDto billing = staffBillingMapper.selectSummary(today);
        int unpaidCnt = 0;
        long todayAmt = 0L;
        if (billing != null) {
            if (billing.getUnpaidCnt() != null) unpaidCnt = billing.getUnpaidCnt();
            if (billing.getTodayAmt() != null) todayAmt = billing.getTodayAmt();
        }
        model.addAttribute("unpaidCnt", unpaidCnt);
        model.addAttribute("todayAmt", todayAmt);

        boolean isTeamLead = hasAuthority(authentication, "A001");
        model.addAttribute("isTeamLead", isTeamLead);
        if (isTeamLead) {
            model.addAttribute("teamMemberCnt", staffMapper.countActiveStaffByDept("D100"));

            ConsultationSummaryDto cnsl = consultationMapper.selectSummary();
            model.addAttribute("followupCnt", cnsl != null ? cnsl.getFollowupCnt() : 0);

            RetentionSummaryDto retention = retentionMapper.selectSummary();
            model.addAttribute("riskCnt", retention != null ? retention.getRiskCnt() : 0);
        }

        return "admin:/dashboard/dashboard-staff";
    }

    private boolean hasAuthority(Authentication auth, String authority) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }
}
