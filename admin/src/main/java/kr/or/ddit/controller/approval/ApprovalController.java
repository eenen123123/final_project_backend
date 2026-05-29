package kr.or.ddit.controller.approval;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import kr.or.ddit.finalProject.dto.approval.ApprovalMasterDto;
import kr.or.ddit.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @PostMapping
    public String postApproval(
            @ModelAttribute ApprovalMasterDto masterDto,
            @RequestParam(required = false, defaultValue = "[]") String approvalLine,
            Authentication authentication) {
        approvalService.submitApproval(authentication.getName(), masterDto, approvalLine);
        return "redirect:/admin/approval";
    }

    @PostMapping("/{aprvlDocSn}/update")
    public String updateApproval(
            @PathVariable Long aprvlDocSn,
            @ModelAttribute ApprovalMasterDto masterDto,
            @RequestParam(required = false, defaultValue = "[]") String approvalLine,
            Authentication authentication) {
        masterDto.setAprvlDocSn(aprvlDocSn);
        approvalService.updateApproval(authentication.getName(), masterDto, approvalLine);
        return "redirect:/admin/approval/" + aprvlDocSn;
    }

}
