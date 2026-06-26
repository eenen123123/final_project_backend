package kr.or.ddit.finalProject.dto.member;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto implements Serializable {

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

    // 클래스 등록 정보 (students 목록 조회 시 서브쿼리로 채워짐)
    private Integer classCnt;     // 등록된 클래스 수
    private String firstInstrNm;  // 대표 강사 이름
    private String firstClassNm;  // 대표 클래스명

    // 학부모 연동 정보 (STUDENT 테이블 JOIN 시 채워짐)
    private String prntUserId;
    private String prntUserName;   // 학부모명
    private String prntTelno;      // 학부모 연락처
    private String prntEmailAddr;  // 학부모 이메일

}
