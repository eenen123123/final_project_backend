package kr.or.ddit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;


@Slf4j
@Controller
@RequestMapping("/admin")
public class StaffController {

    /**
     * 원비 및 수납 관리
     * @return
     */
    @GetMapping("/billing")
    public String getBilling() {
        return "admin:/staff/billing";
    }

    /**
     * 지출 및 영수증 관리
     * @return
     */
    @GetMapping("/expenses")
    public String getExpenses() {
        return "admin:/staff/expenses";
    }

    /**
     * 출결 관리
     */
    @GetMapping("/attendance")
    public String getAttendance() {
        return "admin:/staff/attendance";
    }

    /**
     * 교재 및 물류 관리
     */
    @GetMapping("/logistics")
    public String getLogistics() {
        return "admin:/staff/logistics";
    }

    /**
     * 직원 정보 및 계정 관리
     */
    @GetMapping("/employees")
    public String getEmployees() {
        return "admin:/staff/employees";
    }

    /**
     * 근태 및 휴가 관리
     */
    @GetMapping("/hr/leave")
    public String getHrLeave() {
        return "admin:/staff/hr_leave";
    }

    /** 증명서 발급 관리 */
    @GetMapping("/certificates")
    public String getCertificates() {
        return "admin:/staff/certificates";
    }

    /**
     * 시설 및 업체 관리
     */
    @GetMapping("/facilities")
    public String getFacilities() {
        return "admin:/staff/facilities";
    }

    /**
     * 학부모 알림 발송
     */
    @GetMapping("/notifications/parent")
    public String getNotificationsParent() {
        return "admin:/staff/notifications_parent";
    }

    /**
     * 블랙리스트 관리
     */
    @GetMapping("'/blacklist")
    public String getBlacklist() {
        return "admin:/staff/blacklist";
    }
    
    
    
    
    
    
    
    
    
    
}
