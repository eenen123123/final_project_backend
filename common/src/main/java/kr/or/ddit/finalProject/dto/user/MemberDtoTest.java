package kr.or.ddit.finalProject.dto.user;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDtoTest implements Serializable {
    private String userId; // 사용자ID (PK)
    private String userEnpswd; // 사용자비밀번호 (암호화 저장)
    private String userName; // 사용자명
    private String userEnrrno; // 암호화주민등록번호 (암호화 필수, 원문 저장 금지)
    private String userGndrCd; // 성별코드 (M:남, F:여, U:미확인)
    private LocalDate userBrdt; // 사용자생년월일 (YYYYMMDD)
    private String joinDt; // 가입일시
    private String userTelno; // 사용자전화번호
    private String userEmailAddr; // 사용자이메일주소
    private String userZip; // 사용자우편번호
    private String userAddr; // 사용자주소
    private String userDaddr; // 사용자상세주소
    private String userProfile; // 프로필이미지경로명
    private String regDate; // 등록일시
    private String modDate; // 수정일시
    private String enable; // 사용여부

    private List<String> memRoles; // MemberDto has many roleName

}
