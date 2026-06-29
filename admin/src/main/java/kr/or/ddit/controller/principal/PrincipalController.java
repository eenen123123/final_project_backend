package kr.or.ddit.controller.principal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.or.ddit.finalProject.dto.monitoring.ClassroomGradeStatsDto;
import kr.or.ddit.finalProject.dto.monitoring.ClassroomOverviewDto;
import kr.or.ddit.finalProject.dto.monitoring.ExamScheduleDto;
import kr.or.ddit.finalProject.dto.monitoring.ProgressTrendDto;
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
        // 월별 진도율 추이 → Chart.js dataset 형태로 가공
        List<ProgressTrendDto> trendList = monitoringService.getProgressTrend();
        List<String> progressMonths = trendList.stream()
            .map(ProgressTrendDto::getMon)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        Map<String, List<Double>> trendByClass = new LinkedHashMap<>();
        for (ProgressTrendDto t : trendList) {
            trendByClass.computeIfAbsent(t.getClassNm(), k -> new ArrayList<>());
        }
        // 월 순서에 맞게 값 배열 구성 (해당 월 데이터 없으면 null)
        Map<String, Map<String, Double>> rateMap = trendList.stream()
            .collect(Collectors.groupingBy(
                ProgressTrendDto::getClassNm,
                LinkedHashMap::new,
                Collectors.toMap(ProgressTrendDto::getMon, ProgressTrendDto::getProgressRate)
            ));
        List<Map<String, Object>> progressDatasets = new ArrayList<>();
        for (String classNm : rateMap.keySet()) {
            Map<String, Double> byMon = rateMap.get(classNm);
            List<Double> data = progressMonths.stream()
                .map(m -> byMon.getOrDefault(m, null))
                .collect(Collectors.toList());
            Map<String, Object> ds = new LinkedHashMap<>();
            ds.put("label", classNm);
            ds.put("data", data);
            progressDatasets.add(ds);
        }
        model.addAttribute("progressMonths", progressMonths);
        model.addAttribute("progressDatasets", progressDatasets);

        List<ExamScheduleDto> upcomingExams = monitoringService.getUpcomingExams();
        List<ExamScheduleDto> completedExams = monitoringService.getRecentCompletedExams();
        List<ClassroomGradeStatsDto> gradeStats = monitoringService.getClassroomGradeStats();
        Map<String, Object> gradeDist = monitoringService.getGradeDistribution();
        double totalAvgScore = gradeStats.stream()
            .mapToDouble(ClassroomGradeStatsDto::getAvgScore)
            .average()
            .orElse(0);

        model.addAttribute("classrooms", classrooms);
        model.addAttribute("classroomCnt", classrooms.size());
        model.addAttribute("avgProgress", avgProgress);
        model.addAttribute("upcomingExams", upcomingExams);
        model.addAttribute("upcomingExamCnt", upcomingExams.size());
        model.addAttribute("completedExams", completedExams);
        model.addAttribute("gradeStats", gradeStats);
        model.addAttribute("gradeDist", gradeDist);
        model.addAttribute("totalAvgScore", Math.round(totalAvgScore * 10.0) / 10.0);
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