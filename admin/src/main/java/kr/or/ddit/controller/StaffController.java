package kr.or.ddit.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.or.ddit.finalProject.dto.employee.DepartmentDto;
import kr.or.ddit.finalProject.dto.employee.JobGradeDto;
import kr.or.ddit.finalProject.mapper.StaffMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;


@Slf4j
@Controller
@RequestMapping("/admin")
public class StaffController {
    
    @Autowired
    StaffMapper staffMapper;

    /**
     * 원비 및 수납 관리
     * @return
     */
    @GetMapping("/billing")
    public String getBilling() {
        log.info("getBilling()");
        return "admin:/staff/billing";
    }

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

    /**
     * 직원 정보 및 계정 관리
     */
    @GetMapping("/employees")
    public String getEmployees(Model model) {
        log.info("getEmployees()");

        List<DepartmentDto> departmentlist = staffMapper.selectDepartmentList();
        List<JobGradeDto> jobgradelist = staffMapper.selectJobGradeList();

        model.addAttribute("departmentlist", departmentlist);
        model.addAttribute("jobgradelist", jobgradelist);

        return "admin:/staff/employees";
    }

    /**
     * 근태 및 휴가 관리
     */
    @GetMapping("/hr/leave")
    public String getHrLeave() {
        log.info("getHrLeave()");
        return "admin:/staff/hr_leave";
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
     * 학부모 알림 발송
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
