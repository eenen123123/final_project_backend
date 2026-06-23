package kr.or.ddit.controller.manager;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.or.ddit.finalProject.dto.instructor.journal.InstructorJournalDto;
import kr.or.ddit.finalProject.dto.monitoring.ClassroomOverviewDto;
import kr.or.ddit.finalProject.service.instructor.InstructorJournalService;
import kr.or.ddit.service.InstructorMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ManagerController {

    private final InstructorMonitorService instructorMonitorService;
    private final InstructorJournalService journalService;

    /**
     * 학부모 상담 관리
     */
    @GetMapping("/consultation")
    public String getConsultation() {
        log.info("getConsultation()");
        return "admin:/manager/consultation";
    }

    /**
     * 퇴원 방어 및 유지
     */
    @GetMapping("/retention")
    public String getRetention() {
        log.info("getRetention()");
        return "admin:/manager/retention";
    }

    /**
     * 강사 업무 모니터링
     * - 로그인한 매니저의 팀 강사 카드 + 일일 업무 기록(INSTRUCTOR_JOURNAL) 표시
     * - Z001(원장): mgrUserId=null → 전체 강사 조회
     * - 그 외: mgrUserId=auth.getName() → CONNECT BY로 팀원만 조회
     */
    @GetMapping("/instructors/monitor")
    public String getInstructorsMonitor(
            @RequestParam(required = false) String instrId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String fromDt,
            @RequestParam(required = false) String toDt,
            @RequestParam(required = false, defaultValue = "1") int page,
            Model model,
            Authentication auth) {

        log.info("getInstructorsMonitor()");

        // 요약 지표
        model.addAttribute("activeClassCount",      instructorMonitorService.getActiveClassCount());
        model.addAttribute("totalStudentCount",     instructorMonitorService.getTotalStudentCount());
        model.addAttribute("thisMonthJournalCount", instructorMonitorService.getThisMonthJournalCount());

        // 운영중 강좌 목록 (instrUserId 포함 → 일지 보기 링크에 사용)
        List<ClassroomOverviewDto> activeClassrooms = instructorMonitorService.getActiveClassrooms();
        model.addAttribute("activeClassrooms", activeClassrooms);

        // 선택된 강사명 (강좌 테이블에서 '일지 보기' 클릭 시)
        String selectedInstrNm = "";
        if (instrId != null && !instrId.isBlank()) {
            selectedInstrNm = activeClassrooms.stream()
                    .filter(c -> instrId.equals(c.getInstrUserId()))
                    .map(ClassroomOverviewDto::getInstrUserNm)
                    .findFirst()
                    .orElse("");
        }
        model.addAttribute("selectedInstrNm", selectedInstrNm);

        // 업무 일지: instrId(강좌 클릭 시 강사 userId) → 해당 강사 일지만 / 미선택 → 전체
        String instrUserFilter = (instrId != null && !instrId.isBlank()) ? instrId : null;
        List<InstructorJournalDto> journalList = journalService.retrieveJournalList(
                auth.getName(), true, null, instrUserFilter, keyword, fromDt, toDt, page);
        int totalCount = journalService.retrieveJournalCount(
                auth.getName(), true, null, instrUserFilter, keyword, fromDt, toDt);
        int pageSize   = InstructorJournalService.PAGE_SIZE;
        int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / pageSize));
        int startItem  = totalCount > 0 ? (page - 1) * pageSize + 1 : 0;
        int endItem    = Math.min(page * pageSize, totalCount);

        model.addAttribute("journalList",  journalList);
        model.addAttribute("totalCount",   totalCount);
        model.addAttribute("currentPage",  page);
        model.addAttribute("totalPages",   totalPages);
        model.addAttribute("startItem",    startItem);
        model.addAttribute("endItem",      endItem);

        model.addAttribute("instrId",  instrId  != null ? instrId  : "");
        model.addAttribute("keyword",  keyword  != null ? keyword  : "");
        model.addAttribute("fromDt",   fromDt   != null ? fromDt   : "");
        model.addAttribute("toDt",     toDt     != null ? toDt     : "");

        return "admin:/manager/instructors_monitor";
    }

    /**
     * 원장 승인 요청 관리
     */
    @GetMapping("/approval/request")
    public String getApprovalRequest() {
        log.info("getApprovalRequest()");
        return "admin:/manager/approval_request";
    }

    /**
     * 관리자 권한 운영
     */
    @GetMapping("/settings/manager-permissions")
    public String getManagerPermissions() {
        log.info("getManagerPermissions()");
        return "admin:/manager/manager_permissions";
    }

}
