package kr.or.ddit.finalProject.dto.course;

import lombok.Data;

@Data
public class CourseLectureItem {

    private Long lectureSn;
    private String title;
    private Integer duration;
    private String typeCd;
    private Integer sortOrd;
    private String lockYn;
}
