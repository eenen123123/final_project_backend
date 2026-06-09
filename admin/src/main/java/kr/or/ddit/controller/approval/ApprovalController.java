package kr.or.ddit.controller.approval;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import kr.or.ddit.finalProject.dto.approval.ApprovalMasterDto;
import kr.or.ddit.finalProject.service.NotificationService;
import kr.or.ddit.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;


@Slf4j
@Controller
@RequestMapping("/admin/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;
    private final NotificationService notificationService;

    @PostMapping
    public String postApproval(@ModelAttribute ApprovalMasterDto masterDto,
            @RequestParam(required = false, defaultValue = "[]") String approvalLine,
            Authentication authentication) {
        approvalService.submitApproval(authentication.getName(), masterDto, approvalLine);
        return "redirect:/admin/approval";
    }

    @PostMapping("/{aprvlDocSn}/update")
    public String updateApproval(@PathVariable Long aprvlDocSn,
            @ModelAttribute ApprovalMasterDto masterDto,
            @RequestParam(required = false, defaultValue = "[]") String approvalLine,
            Authentication authentication) {
        masterDto.setAprvlDocSn(aprvlDocSn);
        approvalService.updateApproval(authentication.getName(), masterDto, approvalLine);
        return "redirect:/admin/approval/" + aprvlDocSn;
    }

    @PostMapping("/{aprvlDocSn}/delete")
    public String deleteApproval(@PathVariable Long aprvlDocSn, Authentication authentication) {
        approvalService.deleteApproval(authentication.getName(), aprvlDocSn);

        return "redirect:/admin/approval";
    }

    @PostMapping("/{aprvlDocSn}/cancel")
    public String cancelApproval(@PathVariable Long aprvlDocSn, Authentication authentication) {
        approvalService.cancelApproval(authentication.getName(), aprvlDocSn);


        return "redirect:/admin/approval";
    }

    @PostMapping("/{aprvlDocSn}/approve")
    public ResponseEntity<Void> approveApproval(@PathVariable Long aprvlDocSn,
            @RequestBody Map<String, String> payload, Authentication authentication) {
        String aprvlRsnCn = payload.get("aprvlRsnCn");
        log.info("reason : {}", aprvlRsnCn);

        approvalService.approveApproval(authentication.getName(), aprvlDocSn,
                aprvlRsnCn == null ? "" : aprvlRsnCn);



        return ResponseEntity.ok().build();
    }


}
