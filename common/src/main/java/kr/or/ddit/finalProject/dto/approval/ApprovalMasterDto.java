package kr.or.ddit.finalProject.dto.approval;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * | DRAFT     | 임시저장 (상신 전)     |
 * | --------- | -----------------------|
 * | PENDING   | 상신됨, 결재 진행 중   |
 * | APPROVED  | 최종 승인 완료         |
 * | REJECTED  | 반려됨                 |
 * | CANCELLED | 기안자가 회수/취소     |
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalMasterDto implements Serializable {

    private Long aprvlDocSn; // 기본키(PK) · 자동증가
    private String aprvlDocSj; // 결재문서 제목
    private String aprvlDocCn; // 결재문서 내용
    private String atchFileId; // 공통첨부파일분류
    private String drftUserId; // MEMBER.USER_ID 참조 (FK)
    private LocalDateTime drftRegDt; // 데이터 생성 시점 (자동갱신)
    private ApprovalDocProgressEnum aprvlPrgrsCd; // COM_CD 공통코드 참조
    private LocalDateTime mdfcnDt; // 데이터 수정 시점 (자동갱신)
    private String tmplCd; // COM_CD 공통코드 참조

    private String tmplNm; // 결재 양식명
}
