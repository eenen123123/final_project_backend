package kr.or.ddit.finalProject.dto.approval;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalMasterDto implements Serializable {

    private Long aprvlDocSn; // 기본키(PK) · 자동증가
    private String aprvlDocSj;
    private String aprvlDocCn;
    private String atchFileId; // 공통첨부파일분류
    private String drftUserId; // MEMBER.USER_ID 참조 (FK)
    private LocalDateTime drftRegDt;
    private String aprvlPrgrsCd; // COM_CD 공통코드 참조
    private LocalDateTime mdfcnDt; // 데이터 수정 시점 (자동갱신)
    private String tmplCd;
}
