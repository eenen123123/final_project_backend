package kr.or.ddit.finalProject.dto.classroom;

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
public class ClassroomDto implements Serializable {

    private Long classSn; // PK · 시퀀스
    private String opnrUserId; // MEMBER.USER_ID 참조
    private Long courseSn; // COURSE.COURSE_SN 참조
    private String classNm;
    private String enrlStrtYmd; // 표현형식 YYYYMMDD (CHAR 8)
    private String enrlEndYmd; // 표현형식 YYYYMMDD (CHAR 8), NULL = 무기한
    private String useYn; // 운영여부 Y/N
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
    private String rgtrId; // MEMBER.USER_ID 참조 권장
    private String lastMdfrId;

    // JOIN fields
    private String courseNm;
    private String instrNm; // MEMBER.USER_NM of opnrUserId
    private Integer memberCount;
}