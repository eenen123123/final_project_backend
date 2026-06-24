package kr.or.ddit.finalProject.dto.lecture;

import lombok.Data;

@Data
public class StudentLectureProgressResponse {

    private String userId;
    private String userName;
    private String cmplYn;
    private String cmplDt;
}