package kr.or.ddit.controller.principal;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.or.ddit.finalProject.dto.monitoring.ClassroomOverviewDto;
import kr.or.ddit.service.MonitoringService;
import kr.or.ddit.service.QualityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;


@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class PrincipalController {

    private final QualityService qualityService;
    private final MonitoringService monitoringService;
    

    /**
     * 학생 관리 최종 승인
     * @return
     */
    @GetMapping("/students/approve")
    public String getStudentsApprove() {
        log.info("getStudentsApprove()");
        return "admin:/principal/student_approve";
    }

    /**
     * 매출 및 재무 분석
     * @return
     */
    @GetMapping("/finance")
    public String getFinance() {
        log.info("getFinance()");
        return "admin:/principal/finance";
    }

    /**
     * 결제 최종 승인
     * @return
     */
    @GetMapping("/payments/approve")
    public String getPaymentsApprove() {
        log.info("getPaymentsApprove()");
        return "admin:/principal/payment_approve";
    }

    /**
     * 학사 운영 모니터링
     */
    @GetMapping("/monitoring")
    public String getMonitoring(Model model) {
        log.info("getMonitoring()");
        List<ClassroomOverviewDto> classrooms = monitoringService.getClassroomOverview();
        long avgProgress = Math.round(
            classrooms.stream()
                .mapToDouble(ClassroomOverviewDto::getAvgProgressRate)
                .average()
                .orElse(0)
        );
        model.addAttribute("classrooms", classrooms);
        model.addAttribute("classroomCnt", classrooms.size());
        model.addAttribute("avgProgress", avgProgress);
        return "admin:/principal/monitoring";
    }

    /**
     * 서비스 품질 관리 - 강사별 Q&A 처리율·응답 시간 모니터링
     */
    @GetMapping("/quality")
    public String getQuality(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "30d") String period,
            Model model) {
        log.info("getQuality() period={}", period);
        model.addAttribute("stats",       qualityService.getInstructorQnaStats(period));
        model.addAttribute("summary",     qualityService.getQnaSummary(period));
        model.addAttribute("overdueList", qualityService.getOverdueQnaList());
        model.addAttribute("period",      period);
        return "admin:/principal/quality";
    }
}