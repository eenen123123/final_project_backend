package kr.or.ddit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;


@Slf4j
@Controller
@RequestMapping("/admin")
public class ManagerController {
    
    /**
     * 학부모 상담 관리
     * @return
     */
    @GetMapping("/consultation")
    public String getConsultation() {
        log.info("getConsultation()");
        return "admin:/manager/consultation";
    }

    /**
     * 퇴원 방어 및 유지
     * @return
     */
    @GetMapping("/retention")
    public String getRetention() {
        log.info("getRetention()");
        return "admin:/manager/retention";
    }

    /**
     * 강사 및 업무 모니터링
     * @return
     */
    @GetMapping("/teachers/monitor")
    public String getTeachersMonitor() {
        log.info("getTeachersMonitor()");
        return "admin:/manager/teachers_monitor";
    }

    /**
     * 원장 승인 요청 관리
     * @return
     */
    @GetMapping("/approval/request")
    public String getApprovalRequest() {
        log.info("getApprovalRequest()");
        return "admin:/manager/approval_request";
    }

    /**
     * 관리자 권한 운영
     * @return
     */
    @GetMapping("/settings/manager-permissions")
    public String getManagerPermissions() {
        log.info("getManagerPermissions()");
        return "admin:/manager/manager_permissions";
    }
    
}
