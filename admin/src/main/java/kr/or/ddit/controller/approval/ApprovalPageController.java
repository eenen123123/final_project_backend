package kr.or.ddit.controller.approval;

import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import kr.or.ddit.finalProject.dto.approval.ApprovalDocProgressEnum;
import kr.or.ddit.finalProject.dto.approval.ApprovalLineDto;
import kr.or.ddit.finalProject.dto.approval.ApprovalLineProgressEnum;
import kr.or.ddit.finalProject.dto.approval.ApprovalMasterDto;
import kr.or.ddit.finalProject.dto.approval.ApprovalTemplateDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeInfoDto;
import kr.or.ddit.finalProject.dto.member.AdminMemberDto;
import kr.or.ddit.finalProject.service.member.MemberService;
import kr.or.ddit.service.AdminEmployeeService;
import kr.or.ddit.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@Controller
@RequestMapping("/admin/approval")
@RequiredArgsConstructor
public class ApprovalPageController {

    private final MemberService memberService;
    private final ApprovalService approvalService;
    private final AdminEmployeeService adminEmployeeService;

    @GetMapping
    public String approvalPage(Model model, Authentication authentication) {
        String userId = authentication.getName();
        Map<String, Object> dashboard = approvalService.getApprovalDashboard(userId);
        dashboard.forEach(model::addAttribute);
        return "admin:/approval_template/approval";
    }

    @GetMapping("/new")
    public String newApprovalPage(Model model, Authentication authentication) {
        String userId = authentication.getName();

        AdminMemberDto adminMember = memberService.getAdminUserById(userId);
        List<ApprovalTemplateDto> templates = approvalService.getApprovalTemplateList();
        List<EmployeeInfoDto> approverCandidates =
                adminEmployeeService.getApproverCandidates(userId);

        model.addAttribute("currentUserName", adminMember.getUserName());
        model.addAttribute("currentDeptNm", adminMember.getEmployeeInfo().getDeptNm());
        model.addAttribute("templates", templates);
        model.addAttribute("approverCandidates", approverCandidates);

        return "admin:/approval_template/approval_form";
    }

    @GetMapping("/{aprvlDocSn}")
    public String getApprovalDetail(@PathVariable Long aprvlDocSn, Model model,
            Authentication authentication) {
        log.info("Fetching approval detail for docSn={}", aprvlDocSn);
        ApprovalMasterDto approvalDetail = approvalService.getApprovalDetail(aprvlDocSn);
        List<ApprovalLineDto> approvalLines = approvalService.getApprovalLines(aprvlDocSn);

        ApprovalLineDto myLine = approvalLines.stream()
                .filter(line -> line.getAprvrUserId().equals(authentication.getName())).findFirst()
                .orElse(null);

        // 내 결재선이 존재하는지 여부 (결재자라면 true, 아니면 false)
        boolean isApprover = approvalLines.stream()
                .anyMatch(line -> line.getAprvrUserId().equals(authentication.getName()));

        // 내가 기안자라면 true, 아니면 false
        boolean isDrafter = approvalDetail.getDrftUserId().equals(authentication.getName());

        List<String> statusesToCheck = List.of(ApprovalLineProgressEnum.APPROVED.toString(),
                ApprovalLineProgressEnum.REJECTED.toString(),
                ApprovalLineProgressEnum.SKIPPED.toString());

        // 내가 기안자이면서 아무도 결재를 승인/반려 중이지 않다면 취소 가능
        boolean canCancel = isDrafter && approvalLines.stream()
                .noneMatch(line -> statusesToCheck.contains(line.getAprvlPrgrsCd().name()));


        // 내가 기안자이면서 아직 DRAFT 상태라면 삭제 가능
        boolean canDelete =
                isDrafter && ApprovalDocProgressEnum.DRAFT.equals(approvalDetail.getAprvlPrgrsCd());

        log.info("isApprover={}, isDrafter={}, canCancel={}, canDelete={}", isApprover, isDrafter,
                canCancel, canDelete);

        approvalLines.stream().forEach(line -> log.info("Approval line: {}", line));

        model.addAttribute("isApprover", isApprover);
        model.addAttribute("doc", approvalDetail);
        model.addAttribute("lines", approvalLines);
        model.addAttribute("myLine", myLine);
        model.addAttribute("isDrafter", isDrafter);
        model.addAttribute("canCancel", canCancel);
        model.addAttribute("canDelete", canDelete);


        log.info("line size : {}", approvalLines.size());

        return "admin:/approval_template/approval_detail";
    }

    @GetMapping("/{aprvlDocSn}/edit")
    public String editApproval(@PathVariable Long aprvlDocSn, Model model,
            Authentication authentication) {
        String userId = authentication.getName();
        ApprovalMasterDto doc = approvalService.getApprovalDetail(aprvlDocSn);
        if (!doc.getDrftUserId().equals(userId)) {
            return "redirect:/admin/approval/" + aprvlDocSn;
        }
        AdminMemberDto adminMember = memberService.getAdminUserById(userId);
        List<ApprovalLineDto> existingLines = approvalService.getApprovalLines(aprvlDocSn);
        List<ApprovalTemplateDto> templates = approvalService.getApprovalTemplateList();
        List<EmployeeInfoDto> approverCandidates =
                adminEmployeeService.getApproverCandidates(userId);

        model.addAttribute("doc", doc);
        model.addAttribute("existingLines", existingLines);
        model.addAttribute("templates", templates);
        model.addAttribute("approverCandidates", approverCandidates);
        model.addAttribute("currentUserName", adminMember.getUserName());
        model.addAttribute("currentDeptNm", adminMember.getEmployeeInfo().getDeptNm());

        return "admin:/approval_template/approval_form";
    }

    @ResponseBody
    @GetMapping(value = "/{aprvlDocSn}/content", produces = MediaType.TEXT_HTML_VALUE
            + ";charset=UTF-8")
    public String getApprovalContent(@PathVariable Long aprvlDocSn) {
        ApprovalMasterDto doc = approvalService.getApprovalDetail(aprvlDocSn);
        String content = doc.getAprvlDocCn();
        return content != null ? content : "";
    }

}
