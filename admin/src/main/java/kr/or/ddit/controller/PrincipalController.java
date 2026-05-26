package kr.or.ddit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Slf4j
@Controller
@RequestMapping("/admin")
public class PrincipalController {

    @GetMapping("/settings/permissions")
    public String getPermissions() {
        log.info("getPermissions");
        return "admin:/principal/permission_management";
    }

    @GetMapping("/students/approve")
    public String getStudentsApprove() {
        log.info("getApprove()");
        return "admin:/principal/student_approve";
    }

    @GetMapping("/finance")
    public String getFinance() {
        return "admin:/principal/finance";
    }

    @GetMapping("/payments/approve")
    public String getPaymentsApprove() {
        return "admin:/principal/payment_approve";
    }

    @GetMapping("/monitoring")
    public String getMonitoring() {
        return "admin:/principal/monitoring";
    }

    @GetMapping("/quality")
    public String getQuality() {
        return "admin:/principal/quality";
    }
}