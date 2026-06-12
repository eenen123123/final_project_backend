package kr.or.ddit.finalProject.dto.instructor.profile;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class InstructorRecentPostResponse {

    private Long postSn;
    private String title;
    private LocalDateTime regDt;
    private String boardType;

}
