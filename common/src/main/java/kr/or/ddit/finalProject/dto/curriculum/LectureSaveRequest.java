package kr.or.ddit.finalProject.dto.curriculum;

import lombok.Data;

@Data
public class LectureSaveRequest {
    private String lectureNm;
    private String lectureTypeCd;
    private Integer lectureDuration;
    private String lectureExplnCn;
    private String opnnYn;
    private String lockYn;
    private Integer sortOrd;
    private Long prereqLectureSn;
}
