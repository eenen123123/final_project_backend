package kr.or.ddit.finalProject.dto.instructor;

import java.util.List;

import lombok.Data;

@Data
public class CourseDetailResponse {
    private Long courseSn;
    private String title;
    private String description;
    private Long price;
    private String thumbnailUrl;
    private String category;
    private Integer studentCount;
    private String totalDuration;
    private List<CourseLectureItem> lectures;
}
