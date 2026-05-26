package kr.or.ddit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;


@Slf4j
@Controller
@RequestMapping("/admin")
public class PrincipalController {
    
    /**
     * 관리자 권한 설정
     * @return
     */
    @GetMapping("/settings/permissions")
    public String getPermissions() {
        log.info("getPermissions");
        return "admin:/principal/permission_management";
    }

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
     * @return
     */
    @GetMapping("/monitoring")
    public String getMonitoring() {
        log.info("getMonitoring()");
        return "admin:/principal/monitoring";
    }

    /**
     * 서비스 품질 관리
     * @return
     */
    @GetMapping("/quality")
    public String getQuality() {
        log.info("getQuality()");
        return "admin:/principal/quality";
    }
}