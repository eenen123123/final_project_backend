package kr.or.ddit.finalProject.dto.lecture;

import lombok.Data;

@Data
public class LectureProgressDetailResponse {

    private Long lectureSn;
    private String lectureNm;
    private Integer sortOrd;
    private String opnnYn;
    private String cmplYn;
    private String cmplDt;
}
