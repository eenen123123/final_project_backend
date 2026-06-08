package kr.or.ddit.finalProject.dto.instructor;

import lombok.Data;

@Data
public class InstructorListResponse {

    private Long subjectClId;
    private String subjectClNm;
    private String instrUserId;
    private String userName;
    private String instrProfileImg;

}
