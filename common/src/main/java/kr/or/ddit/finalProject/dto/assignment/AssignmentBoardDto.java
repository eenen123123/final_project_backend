package kr.or.ddit.finalProject.dto.assignment;

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
public class AssignmentBoardDto implements Serializable {

    private Long asgmtSn; // 기본키(PK) · 시퀀스
    private Long classSn;
    private String rgtrUserId;
    private String asgmtSj;
    private String asgmtCn;
    private LocalDateTime sbmtDdlnDt;
    private String asgmtStatCd;
    private String atchFileId;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
    private String rgtrId;
    private String lastMdfrId;
}
