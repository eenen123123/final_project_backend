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
public class LectureDto implements Serializable {

    private Long lectSn; // 기본키(PK) · 시퀀스
    private Long courseSn; // COURSE.COURSE_SN 참조
    private String lectYear;
    private String lectStatCd;
    private String lectStrtYmd;
    private String lectEndYmd;
    private Integer curEnrlCnt;
    private String rgtrId;
    private String lastMdfrId;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
    private String lrnTimeCnt; // 01:12:24
    private String atchFileId;
}