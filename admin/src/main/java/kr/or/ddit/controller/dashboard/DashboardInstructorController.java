package kr.or.ddit.controller.dashboard;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.or.ddit.finalProject.mapper.StaffMapper;
import kr.or.ddit.finalProject.mapper.classroom.ClassroomMapper;
import kr.or.ddit.finalProject.mapper.exam.QuestionMapper;
import kr.or.ddit.finalProject.mapper.instructor.InstructorBoardMapper;
import kr.or.ddit.mapper.ApprovalMapper;
import kr.or.ddit.mapper.InstructorMonitorMapper;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/dashboard/instructor")
@RequiredArgsConstructor
public class DashboardInstructorController {

    private final ApprovalMapper approvalMapper;
    private final InstructorMonitorMapper instructorMonitorMapper;
    private final StaffMapper staffMapper;
    private final ClassroomMapper classroomMapper;
    private final QuestionMapper questionMapper;
    private final InstructorBoardMapper instructorBoardMapper;

    @GetMapping
    public String dashboard(Authentication authentication, Model model) {
        String userId = authentication.getName();

        model.addAttribute("myPendingCnt", approvalMapper.selectMyPendingLines(userId).size());
        model.addAttribute("myClassroomCnt", classroomMapper.countClassroomListByInstructor(userId));
        model.addAttribute("myQuestionCnt", questionMapper.countQuestions(userId, null, null, false));
        int unansweredQnaCnt = instructorBoardMapper.countUnansweredInstructorQna(userId);
        model.addAttribute("unansweredQnaCnt", unansweredQnaCnt);
        model.addAttribute("recentQnaList", instructorBoardMapper.selectRecentUnansweredInstructorQna(userId, 3));

        boolean isTeamLead = hasAuthority(authentication, "T001");
        model.addAttribute("isTeamLead", isTeamLead);

        if (isTeamLead) {
            model.addAttribute("activeClassCnt", instructorMonitorMapper.selectActiveClassCount());
            model.addAttribute("thisMonthJournalCnt", instructorMonitorMapper.selectThisMonthJournalCount());
            model.addAttribute("teamMemberCnt", staffMapper.countActiveStaffByDept("D300"));
        } else {
            model.addAttribute("myJournalCnt", instructorMonitorMapper.selectMyJournalCountThisMonth(userId));
        }

        return "admin:/dashboard/dashboard-instructor";
    }

    private boolean hasAuthority(Authentication auth, String authority) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }
}
