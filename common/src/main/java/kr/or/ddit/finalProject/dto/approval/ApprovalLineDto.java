package kr.or.ddit.finalProject.dto.approval;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * | WAITING     | 아직 내 차례 아님    |
 * | ----------- | ---------------------|
 * | IN_PROGRESS | 현재 내가 결재해야 함|
 * | APPROVED    | 승인함               |
 * | REJECTED    | 반려함               |
 * | SKIPPED     | 건너뜀 (전결 등)     |
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalLineDto implements Serializable {

    private Long aprvlLineSn; // 복합PK(1/2) · 자동증가
    private Long aprvlDocSn; // 복합PK(2/2) 
    private Long aprvlOrdr; // 결재자 순서 (1부터 시작)
    private String aprvrUserId; // MEMBER.USER_ID 참조
    private LocalDateTime aprvlDt; // 결재 처리 시점 (자동갱신)
    private ApprovalLineProgressEnum aprvlPrgrsCd; // COM_CD 공통코드 참조
    private String aprvlRsnCn; // 승인·반려 사유 메모

    private String approverName; // 결재자 이름 (화면 표시용)
    private String jbgrNm;       // 결재자 직급명 (화면 표시용)
    private String deptNm;       // 결재자 부서명 (화면 표시용)
    private Integer sortOrd;     // 직급 정렬순서 (화면 표시용)
    private ApprovalMasterDto doc; // 결재 대기 목록에서 JOIN된 문서 정보
}
