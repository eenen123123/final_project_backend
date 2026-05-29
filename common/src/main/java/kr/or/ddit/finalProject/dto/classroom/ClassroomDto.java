package kr.or.ddit.finalProject.dto.classroom;

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
public class ClassroomDto implements Serializable {

    private Long classSn;       // PK · 시퀀스
    private String opnrUserId;  // MEMBER.USER_ID 참조 (개설자)
    private Long courseSn;      // COURSE.COURSE_SN 참조
    private String classNm;
    private String enrlStrtYmd; // 표현형식 YYYYMMDD (CHAR 8)
    private String enrlEndYmd;  // 표현형식 YYYYMMDD (CHAR 8), NULL = 무기한
    private String classStatCd; // 01=모집중, 02=운영중, 03=종료, 04=대기
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
    private String rgtrId;
    private String lastMdfrId;

}
