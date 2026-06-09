package kr.or.ddit.finalProject.dto.instructor;

import lombok.Data;

@Data
public class InstructorFeaturedCourseResponse {

    private Long courseSn;
    private String courseNm;
    private Integer displayOrder;

}
