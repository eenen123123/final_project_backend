package kr.or.ddit.finalProject.dto.classroom;

import lombok.Data;

@Data
public class StudentDetailDto {
    private String userId;
    private String userName;
    private String userEmail;
    private String userTelno;
    private String userProfile;
    private EnrollStatus enrlStatCd;
}
