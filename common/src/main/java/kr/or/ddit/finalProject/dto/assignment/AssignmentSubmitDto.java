package kr.or.ddit.finalProject.dto.assignment;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentSubmitDto implements Serializable {

    private Long sbmtSn; // 기본키(PK) · 시퀀스
    private Long asgmtSn;
    private String sbmtUserId; // USER_ID(FK)
    private String sbmtCn;
    private Long atchFileId; // 공통첨부파일분류
    private Integer score; // 999.99 (소수점 2자리)
    private String grddYn; // Y:채점완료 / N:미채점
    private LocalDateTime grddDt; // COM_CD 참조
    private LocalDateTime sbmtDt;
    private String sbmtStatCd;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
}