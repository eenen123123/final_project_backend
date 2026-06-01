package kr.or.ddit.controller.approval;

import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import kr.or.ddit.finalProject.dto.approval.ApprovalTemplateDto;
import kr.or.ddit.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;



@Slf4j
@Controller
@RequestMapping("/admin/approval/template")
@RequiredArgsConstructor
public class ApprovalTemplateController {
    private final ApprovalService approvalService;

    @GetMapping
    public String approvalTemplatePage(Model model) {
        List<ApprovalTemplateDto> approvalTemplates = approvalService.getApprovalTemplateList();
        model.addAttribute("approvalTemplates", approvalTemplates);

        return "admin:/approval_template/approval_template";
    }

    @PostMapping
    public String insertNewApprovalTemplate(@RequestParam("tmplCd")
    String tmplCd, @RequestParam("tmplNm")
    String tmplNm, @RequestParam("tmplCn")
    MultipartFile tmplCn, Authentication authentication) {

        log.info("Received new approval template: tmplCd={}, tmplNm={}, tmplCn={}", tmplCd, tmplNm,
                tmplCn.getOriginalFilename());
        String rgtrId = authentication.getName();
        approvalService.insertApprovalTemplate(tmplCd, tmplCn, tmplNm, rgtrId);

        return "redirect:/admin/approval/template";
    }

    @GetMapping("/view")
    @ResponseBody
    public String viewApprovalTemplate(@RequestParam
    String tmplCd) {
        log.info("Viewing approval template: tmplCd={}", tmplCd);
        ApprovalTemplateDto template = approvalService.getApprovalTemplateById(tmplCd);
        return template.getTmplCn();
    }

    @GetMapping("/{tmplCd}")
    @ResponseBody
    public ResponseEntity<String> getTemplateHtml(@PathVariable
    String tmplCd) {
        ApprovalTemplateDto tmpl = approvalService.getApprovalTemplateById(tmplCd);
        if (tmpl == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(tmpl.getTmplCn());
    }


}
