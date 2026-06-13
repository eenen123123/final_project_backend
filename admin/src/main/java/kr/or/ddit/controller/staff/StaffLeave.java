package kr.or.ddit.controller.staff;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin")
public class StaffLeave {
    
    /**
     * 근태 및 휴가 관리
     */
    @GetMapping("/hr/leave")
    public String getHrLeave() {
        log.info("getHrLeave()");
        return "admin:/staff/hr_leave";
    }
}
