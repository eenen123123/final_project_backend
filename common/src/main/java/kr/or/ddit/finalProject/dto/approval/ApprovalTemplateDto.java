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
public class ApprovalTemplateDto implements Serializable {

    private String tmplCd; // 기본키(PK) · 양식코드
    private String tmplNm; // 결재 양식명
    private String tmplCn; // 결재 양식 내용
    private String useYn; // DEFAULT 'Y' 권장
    private String rgtrId; // 최초등록자
    private String lastMdfrId; // 최종수정자
    private LocalDateTime regDt; // 데이터 생성 시점 (자동갱신)
    private LocalDateTime mdfcnDt; // 데이터 수정 시점 (자동갱신)
    private String atchFileId; // 공통첨부파일분류
}
