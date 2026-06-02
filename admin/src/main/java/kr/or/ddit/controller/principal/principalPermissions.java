package kr.or.ddit.controller.principal;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.or.ddit.finalProject.dto.log.LoginLogDto;
import kr.or.ddit.finalProject.service.log.LoginLogService;
import kr.or.ddit.finalProject.service.staff.StaffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class principalPermissions {

    private final StaffService staffService;
    private final LoginLogService loginLogService;

    /**
     * 관리자 권한 설정 페이지로 이동
     * @param model
     * @return
     */
    @GetMapping("/settings/permissions")
    public String getPermissions(Model model) {
        log.info("getPermissions");

        Map<String, LoginLogDto> lastLoginMap = loginLogService.getLastLoginPerUser()
                .stream()
                .collect(Collectors.toMap(LoginLogDto::getUserId, dto -> dto));

        long onlineCount = lastLoginMap.values().stream()
                .filter(dto -> dto.getLogoutDt() == null)
                .count();

        model.addAttribute("employeeList", staffService.retrieveEmployeeList());
        model.addAttribute("lastLoginMap", lastLoginMap);
        model.addAttribute("onlineCount", onlineCount);
        model.addAttribute("departmentList", staffService.retrieveDepartmentList());
        model.addAttribute("jobGradeList", staffService.retrieveJobGradeList());
        return "admin:/principal/permission_management";
    }
}
