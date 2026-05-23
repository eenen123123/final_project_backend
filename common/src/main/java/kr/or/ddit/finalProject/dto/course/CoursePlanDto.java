package kr.or.ddit.finalProject.dto.course;

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
public class CoursePlanDto implements Serializable {

    private Long planSn; // 기본키(PK) · 시퀀스
    private Long courseSn;
    private String drftUserId; // USER_ID(FK)
    private String lrnGoalCn;
    private String wklyPlanCn; // 주차별 강의 계획
    private String refBookCn;
    private String atchFileId; // 공통첨부파일분류
    private String rgtrId;
    private String lastMdfrId;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
}