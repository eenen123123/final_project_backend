package kr.or.ddit.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.or.ddit.finalProject.dto.approval.ApprovalLineDto;
import kr.or.ddit.finalProject.dto.approval.ApprovalLineProgressEnum;
import kr.or.ddit.finalProject.dto.approval.ApprovalMasterDto;
import kr.or.ddit.finalProject.dto.approval.ApprovalTemplateDto;
import kr.or.ddit.finalProject.dto.approval.ApprovalDocProgressEnum;
import kr.or.ddit.finalProject.dto.employee.EmployeeInfoDto;
import kr.or.ddit.finalProject.dto.member.AdminMemberDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.dto.notification.NotificationType;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.service.NotificationService;
import kr.or.ddit.finalProject.service.member.MemberService;
import kr.or.ddit.mapper.ApprovalMapper;
import kr.or.ddit.service.event.ApprovalCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalMapper approvalMapper;
    private final MemberService memberService;
    private final AdminEmployeeService adminEmployeeService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;

    /**
     * 결재 양식 리스트 조회
     * 
     * @return 결재 양식 리스트
     */
    public List<ApprovalTemplateDto> getApprovalTemplateList() {
        return approvalMapper.selectApprovalTemplateList();
    }

    public int insertApprovalTemplate(String tmplCd, MultipartFile tmplCn, String tmplNm,
            String rgtrId) {
        log.info("Inserting new approval template: tmplCd={}, tmplNm={}, tmplCn={}", tmplCd, tmplNm,
                tmplCn.getOriginalFilename());
        if (tmplCn.isEmpty()) {
            log.warn("Template content file is empty for tmplCd={}", tmplCd);
            return 0; // 파일이 비어있으면 삽입하지 않음
        }
        // html 파일이 아닐 경우 저장 안함
        if (!tmplCn.getOriginalFilename().endsWith(".html")) {
            log.warn("Uploaded file is not an HTML file for tmplCd={}", tmplCd);
            return 0; // html 파일이 아니면 삽입하지 않음
        }

        ApprovalTemplateDto approvalTemplateDto = new ApprovalTemplateDto();
        approvalTemplateDto.setTmplCd(tmplCd.trim());
        approvalTemplateDto.setTmplNm(tmplNm);
        try {
            approvalTemplateDto.setTmplCn(new String(tmplCn.getBytes(), "UTF-8")); // 실제 html 파일의 내용을 저장
        } catch (Exception e) {
            log.error("Error reading template content", e);
            throw new FinalProjectException(ErrorCode.FILE_READ_ERROR);
        }
        approvalTemplateDto.setRgtrId(rgtrId);
        approvalTemplateDto.setLastMdfrId(rgtrId); // 최초 등록자와 최종 수정자를 동일하게 설정
        approvalTemplateDto.setUseYn("Y"); // 기본값으로 사용 여부를 'Y'로 설정
        return approvalMapper.insertApprovalTemplate(approvalTemplateDto);
    }

    public ApprovalTemplateDto getApprovalTemplateById(String tmplCd) {
        log.info("Fetching approval template by ID: tmplCd={}", tmplCd);
        return approvalMapper.selectApprovalTemplateById(tmplCd);
    }

    public List<ApprovalLineDto> getDefaultApprovalLinesByUserId(String userId) {
        // 기본 결재선은 일단은 팀장 - 원장
        // 팀의 코드로 팀장을 구함
        AdminMemberDto memberDto = memberService.getAdminUserById(userId);
        String deptCd = memberDto.getEmployeeInfo().getDeptCd();
        EmployeeInfoDto teamLeader = adminEmployeeService.getTeamLeaderByDeptCd(deptCd);
        List<ApprovalLineDto> approvalLines = new LinkedList<>();
        if (teamLeader != null) {
            ApprovalLineDto teamLeaderLine = new ApprovalLineDto();
            teamLeaderLine.setAprvrUserId(teamLeader.getUserId());
            teamLeaderLine.setApproverName(teamLeader.getUserId()); // 실제로는 이름을 가져와야 하지만, 일단은 userId로 설정
            teamLeaderLine.setAprvlOrdr(1l); // 팀장이 첫 번째 결재자
            teamLeaderLine.setAprvlPrgrsCd(ApprovalLineProgressEnum.WAITING);
            teamLeaderLine.setJbgrNm(teamLeader.getJbgrNm()); // 직급명 설정
            approvalLines.add(teamLeaderLine);
        }
        // 원장
        ApprovalLineDto directorLine = new ApprovalLineDto();
        MemberDto director = memberService.getMemberByUserId("testuser01"); // 원장 계정은 일단 하드코딩
        directorLine.setAprvrUserId(director.getUserId());
        directorLine.setApproverName(director.getUserName()); // 실제로는 이름
        directorLine.setAprvlOrdr(2l); // 원장이 두 번째 결재자
        directorLine.setAprvlPrgrsCd(ApprovalLineProgressEnum.WAITING);
        directorLine.setJbgrNm("원장"); // 직급명도 하드코딩
        approvalLines.add(directorLine);


        return approvalLines;
    }

    public Map<String, Object> getApprovalDashboard(String userId) {
        List<ApprovalMasterDto> myDocs = approvalMapper.selectMyDocs(userId);
        List<ApprovalLineDto> myPendingLines = approvalMapper.selectMyPendingLines(userId);
        List<ApprovalLineDto> myProcessedLines = approvalMapper.selectMyProcessedLines(userId);

        List<ApprovalMasterDto> draftDocs = myDocs.stream()
                .filter(d -> ApprovalDocProgressEnum.DRAFT.equals(d.getAprvlPrgrsCd()))
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("myDocs", myDocs);
        data.put("myPendingLines", myPendingLines);
        data.put("draftDocs", draftDocs);
        data.put("totalCount", myDocs.size());
        data.put("pendingCount", myDocs.stream()
                .filter(d -> ApprovalDocProgressEnum.PENDING.equals(d.getAprvlPrgrsCd())).count());
        data.put("approvedCount", myDocs.stream()
                .filter(d -> ApprovalDocProgressEnum.APPROVED.equals(d.getAprvlPrgrsCd())).count());
        data.put("rejectedCount", myDocs.stream()
                .filter(d -> ApprovalDocProgressEnum.REJECTED.equals(d.getAprvlPrgrsCd())).count());
        data.put("myApprovalCount", myPendingLines.size());
        data.put("draftCount", draftDocs.size());
        data.put("myProcessedLines", myProcessedLines);
        return data;
    }

    @Transactional
    public Long submitApproval(String userId, ApprovalMasterDto master, String approvalLineJson) {
        master.setDrftUserId(userId);
        approvalMapper.insertApprovalMaster(master);

        try {
            List<Map<String, Object>> lineData = objectMapper.readValue(approvalLineJson,
                    new TypeReference<List<Map<String, Object>>>() {});
            boolean isPending = ApprovalDocProgressEnum.PENDING.equals(master.getAprvlPrgrsCd());
            for (int i = 0; i < lineData.size(); i++) {
                Map<String, Object> data = lineData.get(i);
                ApprovalLineDto line = new ApprovalLineDto();
                line.setAprvlDocSn(master.getAprvlDocSn());
                line.setAprvrUserId((String) data.get("aprvrUserId"));
                line.setAprvlOrdr((long) (i + 1));
                line.setAprvlPrgrsCd(isPending && i == 0 ? ApprovalLineProgressEnum.IN_PROGRESS
                        : ApprovalLineProgressEnum.WAITING);
                approvalMapper.insertApprovalLine(line);
            }
            if (isPending && !lineData.isEmpty()) {
                notificationService.sendNotification(
                        (String) lineData.get(0).get("aprvrUserId"),
                        userId, NotificationType.APPROVAL,
                        "결재 요청: " + master.getAprvlDocSj(),
                        "/admin/approval/" + master.getAprvlDocSn());
            }
        } catch (Exception e) {
            throw new FinalProjectException(ErrorCode.FILE_READ_ERROR);
        }

        return master.getAprvlDocSn();
    }


    @Transactional
    public void updateApproval(String userId, ApprovalMasterDto master, String approvalLineJson) {
        ApprovalMasterDto existing =
                approvalMapper.selectApprovalMasterByDocSn(master.getAprvlDocSn());
        if (existing == null || !existing.getDrftUserId().equals(userId)) {
            throw new FinalProjectException(ErrorCode.APPROVAL_NOT_FOUND);
        }
        approvalMapper.updateApprovalMaster(master);
        approvalMapper.deleteApprovalLinesByDocSn(master.getAprvlDocSn(),
                ApprovalLineProgressEnum.CANCELED.name());

        try {
            List<Map<String, Object>> lineData = objectMapper.readValue(approvalLineJson,
                    new TypeReference<List<Map<String, Object>>>() {});
            boolean isPending = ApprovalDocProgressEnum.PENDING.equals(master.getAprvlPrgrsCd());
            for (int i = 0; i < lineData.size(); i++) {
                Map<String, Object> data = lineData.get(i);
                ApprovalLineDto line = new ApprovalLineDto();
                line.setAprvlDocSn(master.getAprvlDocSn());
                line.setAprvrUserId((String) data.get("aprvrUserId"));
                line.setAprvlOrdr((long) (i + 1));
                line.setAprvlPrgrsCd(isPending && i == 0 ? ApprovalLineProgressEnum.IN_PROGRESS
                        : ApprovalLineProgressEnum.WAITING);
                approvalMapper.insertApprovalLine(line);
            }
            if (isPending && !lineData.isEmpty()) {
                notificationService.sendNotification(
                        (String) lineData.get(0).get("aprvrUserId"),
                        userId, NotificationType.APPROVAL,
                        "결재 요청: " + master.getAprvlDocSj(),
                        "/admin/approval/" + master.getAprvlDocSn());
            }
        } catch (Exception e) {
            throw new FinalProjectException(ErrorCode.FILE_READ_ERROR);
        }
    }

    public ApprovalMasterDto getApprovalDetail(Long aprvlDocSn) {
        ApprovalMasterDto master = approvalMapper.selectApprovalMasterByDocSn(aprvlDocSn);
        if (master == null) {
            throw new FinalProjectException(ErrorCode.APPROVAL_NOT_FOUND);
        }
        return master;
    }

    public List<ApprovalLineDto> getApprovalLines(Long aprvlDocSn) {
        return approvalMapper.selectApprovalLinesByDocSn(aprvlDocSn);
    }

    @Transactional
    public void deleteApproval(String userId, Long aprvlDocSn) {

        ApprovalMasterDto existing = approvalMapper.selectApprovalMasterByDocSn(aprvlDocSn);
        List<ApprovalLineDto> lines = approvalMapper.selectApprovalLinesByDocSn(aprvlDocSn);

        // 기존 문서가 존재하지 않거나, 현재 사용자가 기안자가 아닌 경우 예외 발생
        if (existing == null || !existing.getDrftUserId().equals(userId)) {
            throw new FinalProjectException(ErrorCode.APPROVAL_NOT_FOUND);

        }

        // 문서가 DRAFT 상태가 아닌 경우 예외 발생
        if (!existing.getAprvlPrgrsCd().equals(ApprovalDocProgressEnum.DRAFT)) {
            throw new FinalProjectException(ErrorCode.APPROVAL_NOT_DRAFT);
        }

        int result = approvalMapper.deleteApprovalMaster(aprvlDocSn,
                ApprovalDocProgressEnum.CANCELED.name());
        if (result == 0) {
            throw new FinalProjectException(ErrorCode.APPROVAL_DELETE_FAILED);
        }

        List<ApprovalLineDto> activeLines = lines.stream()
                .filter(line -> !line.getAprvlPrgrsCd().equals(ApprovalLineProgressEnum.CANCELED))
                .collect(Collectors.toList());
        if (activeLines.size() > 0) {
            int resultLines = approvalMapper.deleteApprovalLinesByDocSn(aprvlDocSn,
                    ApprovalLineProgressEnum.CANCELED.name());
            if (resultLines == 0) {
                throw new FinalProjectException(ErrorCode.APPROVAL_DELETE_FAILED);
            }
        }

    }

    @Transactional
    public void cancelApproval(String name, Long aprvlDocSn) {
        // 결재 문서가 존재해야 하고, 본인의 결재여야 하며,  아무도 결재를 승인하지 않은 상태여야 함
        ApprovalMasterDto existing = approvalMapper.selectApprovalMasterByDocSn(aprvlDocSn);
        if (existing == null) {
            throw new FinalProjectException(ErrorCode.APPROVAL_NOT_FOUND);
        }

        if (!existing.getDrftUserId().equals(name)) {
            throw new FinalProjectException(ErrorCode.APPROVAL_NOT_AUTHORIZED);
        }

        if (!ApprovalDocProgressEnum.PENDING.equals(existing.getAprvlPrgrsCd())) {
            throw new FinalProjectException(ErrorCode.CANNOT_CANCEL_APPROVAL);
        }

        List<ApprovalLineDto> lines = approvalMapper.selectApprovalLinesByDocSn(aprvlDocSn);

        boolean hasApproved = lines.stream()
                .anyMatch(line -> line.getAprvlPrgrsCd().equals(ApprovalLineProgressEnum.APPROVED));

        if (hasApproved) {
            throw new FinalProjectException(ErrorCode.CANNOT_CANCEL_APPROVAL);
        }

        int result = approvalMapper.deleteApprovalMaster(aprvlDocSn,
                ApprovalDocProgressEnum.CANCELED.name());

        if (result == 0) {
            throw new FinalProjectException(ErrorCode.APPROVAL_DELETE_FAILED);
        }
        if (lines.size() > 0) {
            int resultLines = approvalMapper.deleteApprovalLinesByDocSn(aprvlDocSn,
                    ApprovalLineProgressEnum.CANCELED.name());
            if (resultLines == 0) {
                throw new FinalProjectException(ErrorCode.APPROVAL_DELETE_FAILED);
            }
        }
    }

    @Transactional
    public void approveApproval(String name, Long aprvlDocSn, String aprvlRsnCn) {
        ApprovalMasterDto master = approvalMapper.selectApprovalMasterByDocSn(aprvlDocSn);
        if (master == null) {
            throw new FinalProjectException(ErrorCode.APPROVAL_NOT_FOUND);
        }

        if (!ApprovalDocProgressEnum.PENDING.equals(master.getAprvlPrgrsCd())) {
            throw new FinalProjectException(ErrorCode.CANNOT_APPROVE_APPROVAL);
        }

        List<ApprovalLineDto> lines = approvalMapper.selectApprovalLinesByDocSn(aprvlDocSn);

        ApprovalLineDto myLine = lines.stream()
                .filter(line -> line.getAprvrUserId().equals(name)
                        && ApprovalLineProgressEnum.IN_PROGRESS.equals(line.getAprvlPrgrsCd()))
                .findFirst().orElse(null);

        if (myLine == null) {
            throw new FinalProjectException(ErrorCode.CANNOT_APPROVE_APPROVAL);
        }

        // 현재 결재선을 승인으로 변경
        myLine.setAprvlPrgrsCd(ApprovalLineProgressEnum.APPROVED);
        myLine.setAprvlRsnCn(aprvlRsnCn);
        int result = approvalMapper.updateApprovalLine(myLine);
        if (result == 0) {
            throw new FinalProjectException(ErrorCode.FAILED_TO_APPROVE_APPROVAL);
        }

        // 다음 결재선이 있다면, 다음 결재선을 진행중으로 변경
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).getAprvlLineSn().equals(myLine.getAprvlLineSn())) {
                if (i + 1 < lines.size()) {
                    ApprovalLineDto nextLine = lines.get(i + 1);
                    nextLine.setAprvlPrgrsCd(ApprovalLineProgressEnum.IN_PROGRESS);
                    int resultNext = approvalMapper.updateApprovalLine(nextLine);
                    if (resultNext == 0) {
                        throw new FinalProjectException(ErrorCode.FAILED_TO_APPROVE_APPROVAL);
                    }
                    // 다음 결재자에게 알림
                    notificationService.sendNotification(
                            nextLine.getAprvrUserId(), name,
                            NotificationType.APPROVAL,
                            "결재 요청: " + master.getAprvlDocSj(),
                            "/admin/approval/" + aprvlDocSn);
                } else {
                    // 더 이상 결재선이 없으면, 문서 상태를 승인으로 변경
                    master.setAprvlPrgrsCd(ApprovalDocProgressEnum.APPROVED);
                    int resultMaster = approvalMapper.updateApprovalMaster(master);
                    if (resultMaster == 0) {
                        throw new FinalProjectException(ErrorCode.FAILED_TO_APPROVE_APPROVAL);
                    }
                    // 기안자에게 최종 승인 알림
                    notificationService.sendNotification(
                            master.getDrftUserId(), name,
                            NotificationType.APPROVAL,
                            "결재 승인됨: " + master.getAprvlDocSj(),
                            "/admin/approval/" + aprvlDocSn);
                    eventPublisher.publishEvent(new ApprovalCompletedEvent(master.getAprvlDocSn()));
                }
                break;
            }
        }


    }
}
