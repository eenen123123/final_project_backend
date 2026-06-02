package kr.or.ddit.controller.principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin")
public class principalPermissions {

    /**
     * 관리자 권한 설정
     * @return
     */
    @GetMapping("/settings/permissions")
    public String getPermissions() {
        log.info("getPermissions");
        return "admin:/principal/permission_management";
    }
}
