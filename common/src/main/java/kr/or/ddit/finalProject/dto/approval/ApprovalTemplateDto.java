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

    private String tmplCd;
    private String tmplNm; // 결재 양식명
    private String tmplCn;
    private String useYn; // DEFAULT 'Y' 권장
    private String rgtrId;
    private String lastMdfrId;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
    private String atchFileId; // 공통첨부파일분류
}
