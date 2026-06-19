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

    // 목록 조회용 집계 (SQL에서 세팅)
    private int sbmtCnt;
    private int totMemberCnt;

    // 홈 대시보드용 계산값 (서비스에서 세팅, DB 컬럼 아님)
    private int daysUntil;
}
