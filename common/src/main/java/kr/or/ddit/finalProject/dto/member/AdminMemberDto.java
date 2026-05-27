package kr.or.ddit.finalProject.dto.member;

import java.time.LocalDate;
import java.time.LocalDateTime;
import kr.or.ddit.finalProject.dto.employee.EmployeeInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMemberDto {
    private String userId; // 기본키 (PK)
    private String userEnpswd; // 암호화된 비번
    private String userName; // 사용자명
    private String userEnrrno; // AES-256 암호화 필수
    private String userGndrCd; // M:남성 F:여성 U:미확인
    private LocalDate userBrdt;
    private LocalDateTime joinDt; // 가입 시각
    private String userTelno; // (9)99-(9)999-9999 형식
    private String userEmailAddr;
    private String userZip;
    private String userAddr;
    private String userDaddr;
    private String userProfile; // 이미지 URL/경로
    private LocalDateTime regDate; // 데이터 생성 시점
    private LocalDateTime modDate; // 데이터 수정 시점
    private String enable;

    private String userRole; // 사용자 권한 (예: ROLE_USER, ROLE_ADMIN 등)

    private EmployeeInfoDto employeeInfo; // 직원 정보 (EmployeeInfoDto)
}
