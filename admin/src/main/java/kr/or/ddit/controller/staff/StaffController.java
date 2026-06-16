package kr.or.ddit.controller.staff;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.or.ddit.finalProject.service.staff.StaffService;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@Controller
@RequestMapping("/admin")
public class StaffController {

    @Autowired
    PasswordEncoder passwordEncoder;
    
    @Autowired
    StaffService staffService;

    /**
     * 지출 및 영수증 관리
     * @return
     */
    @GetMapping("/expenses")
    public String getExpenses() {
        log.info("getExpenses()");
        return "admin:/staff/expenses";
    }

    /**
     * 출결 관리
     */
    @GetMapping("/attendance")
    public String getAttendance() {
        log.info("getAttendance()");
        return "admin:/staff/attendance";
    }

    /**
     * 교재 및 물류 관리
     */
    @GetMapping("/logistics")
    public String getLogistics() {
        log.info("getLogistics()");
        return "admin:/staff/logistics";
    }


    /** 증명서 발급 관리 */
    @GetMapping("/certificates")
    public String getCertificates() {
        log.info("getCertificates()");
        return "admin:/staff/certificates";
    }

    /**
     * 시설 및 업체 관리
     */
    @GetMapping("/facilities")
    public String getFacilities() {
        log.info("getFacilities()");
        return "admin:/staff/facilities";
    }

    /**
     * 알림 발송
     */
    @GetMapping("/notifications/parent")
    public String getNotificationsParent() {
        log.info("getNotificationsParent()");
        return "admin:/staff/notifications_parent";
    }

    /**
     * 블랙리스트 관리
     */
    @GetMapping("/blacklist")
    public String getBlacklist() {
        log.info("getBlacklist()");
        return "admin:/staff/blacklist";
    }
    
    
    
    
    
    
    
    
    
    
}
