package kr.or.ddit.finalProject.dto.approval;

/**
 * | DRAFT     | 임시저장 (상신 전)     |
 * | --------- | -----------------------|
 * | PENDING   | 상신됨, 결재 진행 중   |
 * | APPROVED  | 최종 승인 완료         |
 * | REJECTED  | 반려됨                 |
 * | CANCELLED | 기안자가 회수/취소     |
 */
public enum ApprovalDocProgressEnum {
    DRAFT, PENDING, APPROVED, REJECTED, CANCELLED

}
