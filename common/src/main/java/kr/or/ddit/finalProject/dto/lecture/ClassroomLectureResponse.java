package kr.or.ddit.finalProject.dto.lecture;

import lombok.Data;

@Data
public class ClassroomLectureResponse {

    private Long lectureSn;
    private String lectureNm;
    private Integer lectureDuration;
    private String lectureTypeCd;
    private Integer sortOrd;
    private String opnnYn;
    private String lockYn;
    private int cmplCnt;
    private int totMemberCnt;
}