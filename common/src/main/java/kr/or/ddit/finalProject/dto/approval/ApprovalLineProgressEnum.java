package kr.or.ddit.finalProject.dto.approval;

/**
 * | WAITING     | 아직 내 차례 아님    |
 * | ----------- | ---------------------|
 * | IN_PROGRESS | 현재 내가 결재해야 함|
 * | APPROVED    | 승인함               |
 * | REJECTED    | 반려함               |
 * | SKIPPED     | 건너뜀 (전결 등)     |
 */
public enum ApprovalLineProgressEnum {
    WAITING, IN_PROGRESS, APPROVED, REJECTED, SKIPPED
}
