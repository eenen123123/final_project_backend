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
    private String enrlStrtYmd; // 표현형식 YYYY-MM-DD
    private String enrlEndYmd; // NULL = 무기한
    private String classStatCd; // COM_CD 공통코드 참조
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
    private String rgtrId; // MEMBER.USER_ID 참조 권장
    private String lastMdfrId;
}