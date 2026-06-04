package kr.or.ddit.finalProject.dto.curriculum;

import lombok.Data;

@Data
public class CourseSaveRequest {
    private String courseNm;
    private String courseExplnCn;
    private String opnnYn;
    private Integer sortOrd;
    private Long prereqCourseSn;
}
