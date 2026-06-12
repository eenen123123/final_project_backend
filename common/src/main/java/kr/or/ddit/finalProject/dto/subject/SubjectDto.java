package kr.or.ddit.finalProject.dto.subject;

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
public class SubjectDto implements Serializable {

    private Long subjId; // 기본키(PK) · 시퀀스
    private Long subjClId;
    private String subjNm;
    private String subjCd;
    private String subjExplnCn;
    private String rgtrId;
    private String lastMdfrId;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
}
