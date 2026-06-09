package kr.or.ddit.finalProject.dto.instructor;

import lombok.Data;

@Data
public class InstructorListResponse {

    private Long subjectClId;
    private String subjectClNm;
    private String instrUuid;
    private String userName;
    private String instrProfileImg;

}
