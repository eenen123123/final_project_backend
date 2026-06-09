package kr.or.ddit.finalProject.dto.instructor;

import lombok.Data;

@Data
public class InstructorPublicCourseResponse {
    private Long courseSn;
    private String title;
    private Long price;
    private String thumbnailUrl;
    private String category;
    private Double rating;       // 현재 스키마에 없음 — null 반환
    private Integer studentCount;
}
