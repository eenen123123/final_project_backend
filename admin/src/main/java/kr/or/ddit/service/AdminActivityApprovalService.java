package kr.or.ddit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.or.ddit.finalProject.dto.approval.ApprovalDocProgressEnum;
import kr.or.ddit.finalProject.dto.approval.ApprovalLineDto;
import kr.or.ddit.finalProject.dto.approval.ApprovalMasterDto;
import kr.or.ddit.finalProject.dto.approval.ApprovalTemplateDto;
import kr.or.ddit.finalProject.dto.member.AdminMemberDto;
import kr.or.ddit.finalProject.dto.staff.AdminActivityType;
import kr.or.ddit.finalProject.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminActivityApprovalService {

    private final ApprovalService approvalService;
    private final MemberService memberService;
    private final ObjectMapper objectMapper;

    /**
     * 결재 요청 생성 + JSON 페이로드를 APRVL_DOC_CN에 저장.
     * 실제 직원/학생 처리는 결재 완료(APPROVED) 후 AdminActivityExecutionService가 실행함.
     *
     * @param actorUserId   요청자 ID
     * @param activityType  처리 유형
     * @param displaySummary 결재 제목에 표시할 요약 (예: "홍길동 (EMP2025001)")
     * @param payloadData   실행 시 필요한 데이터 (DTO 직렬화 맵)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void submitForApproval(String actorUserId, AdminActivityType activityType,
                                  String displaySummary, Map<String, Object> payloadData) {

        String tmplCd = activityType.getTmplCd();

        AdminMemberDto actor = memberService.getAdminUserById(actorUserId);
        String actorName = actor != null ? actor.getUserName() : actorUserId;

        // APRVL_DOC_CN에 저장할 JSON 봉투 (systemPayload 플래그로 시스템 생성 문서 식별)
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("systemPayload", true);
        envelope.put("actionType", activityType.name());
        envelope.put("actorUserId", actorUserId);
        envelope.put("displayText", String.format(
            "담당자: %s (%s)\n처리 내용: %s\n처리 대상: %s",
            actorName, actorUserId, activityType.getLabel(), displaySummary));
        envelope.put("data", payloadData);

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(envelope);
        } catch (Exception e) {
            throw new RuntimeException("페이로드 직렬화 실패: " + e.getMessage(), e);
        }

        ApprovalMasterDto master = ApprovalMasterDto.builder()
            .aprvlDocSj("[결재 요청] " + activityType.getLabel() + " — " + displaySummary)
            .aprvlDocCn(payloadJson)
            .aprvlPrgrsCd(ApprovalDocProgressEnum.PENDING)
            .tmplCd(tmplCd)
            .build();

        List<ApprovalLineDto> lines = approvalService.getDefaultApprovalLinesByUserId(actorUserId);
        String lineJson = toLineJson(lines);

        approvalService.submitApproval(actorUserId, master, lineJson);
        log.info("[AdminActivity] 결재 요청 생성: actorId={}, type={}, target={}",
            actorUserId, activityType, displaySummary);
    }

    private String toLineJson(List<ApprovalLineDto> lines) {
        try {
            List<Map<String, Object>> simplified = new ArrayList<>();
            for (ApprovalLineDto line : lines) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("aprvrUserId", line.getAprvrUserId());
                simplified.add(entry);
            }
            return objectMapper.writeValueAsString(simplified);
        } catch (Exception e) {
            throw new RuntimeException("결재선 직렬화 실패: " + e.getMessage(), e);
        }
    }

}
