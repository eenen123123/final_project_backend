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
public class CourseSatisfactionDto implements Serializable {

    private Long stsfnSn; // 기본키(PK) · 자동증가
    private Long courseSn;
    private String stdUserId; // MEMBER.USER_ID 참조
    private Integer courseStsfnScore; // 1~5 (1점 척도 기준)
    private Integer instrStsfnScore; // 1~5 (1점 척도 기준)
    private Integer etcScore; // 1~5 (1점 척도 기준)
    private String revwCn;
    private String pubYn; // Y:공개 / N:비공개
    private String rgtrId;
    private String lastMdfrId;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
}