package kr.or.ddit.finalProject.dto.lecture;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LectureProgressDto {

    private Long lectureSn;
    private String lectureNm;
    private Integer lectureDuration;
    private String lectureTypeCd;
    private Integer sortOrd;

    private int cmplCnt;
    private int totMemberCnt;
}
