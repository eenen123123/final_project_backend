package kr.or.ddit.finalProject.dto.instructor;

import java.util.List;

import lombok.Data;

@Data
public class InstructorDetailResponse {

    private String instrUuid;
    private String userName;
    private String instrIntro;
    private String instrProfileImg;
    private String subject;
    private Integer lectureCount;
    private List<InstructorCareerDto> careers;
    private List<InstructorCareerDto> books;

}
