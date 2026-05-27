package kr.or.ddit.finalProject.dto.exam;

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
public class AnswerSubmitDto implements Serializable {

    private Long sbmtAnswSn; // 기본키(PK) · 시퀀스
    private Long qstnSn;
    private String stdUserId;
    private String sbmtAnswCn;
    private LocalDateTime sbmtDt;
    private String grdgRsltCd; // COM_CD 공통코드 참조
    private String grdgUserId;
    private LocalDateTime grdgDt;
    private LocalDateTime regDt;
    private String lastMdfrId;
}
