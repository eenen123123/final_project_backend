package kr.or.ddit.finalProject.dto.user;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto implements Serializable {
    private String userId; // 사용자ID (PK)
    private String userEnpswd; // 사용자비밀번호 (암호화 저장)
    private String userNm; // 사용자명
    private String userEnrrno; // 암호화주민등록번호 (암호화 필수, 원문 저장 금지)
    private String userGndrCd; // 성별코드 (M:남, F:여, U:미확인)
    private String userBrdt; // 사용자생년월일 (YYYYMMDD)
    private String joinDt; // 가입일시
    private String userTelno; // 사용자전화번호
    private String userEmlAddr; // 사용자이메일주소
    private String userZip; // 사용자우편번호
    private String userAddr; // 사용자주소
    private String userDaddr; // 사용자상세주소
    private String prflImgPathNm; // 프로필이미지경로명
    private String regDt; // 등록일시
    private String mdfcnDt; // 수정일시

    private String userRole; // 사용자 역할 (예: ADMIN, USER, GUEST 등)

    private MemberRoleDto memberRoleDto; // 회원 권한 정보

    /*
     * -- 테이블 코멘트
     * COMMENT ON TABLE MEMBER IS '사용자관리';
     * 
     * -- 컬럼 코멘트
     * USER_ID IS '사용자ID';
     * USER_ENPSWD IS '사용자비밀번호 (암호화 저장)';
     * USER_NM IS '사용자명';
     * USER_ENRRNO IS '암호화주민등록번호 (암호화 필수, 원문 저장 금지)';
     * USER_GNDR_CD IS '성별코드 (M:남, F:여, U:미확인)';
     * USER_BRDT IS '사용자생년월일 (YYYYMMDD)';
     * JOIN_DT IS '가입일시';
     * USER_TELNO IS '사용자전화번호';
     * USER_EML_ADDR IS '사용자이메일주소';
     * USER_ZIP IS '사용자우편번호';
     * USER_ADDR IS '사용자주소';
     * USER_DADDR IS '사용자상세주소';
     * PRFL_IMG_PATH_NM IS '프로필이미지경로명';
     * REG_DT IS '등록일시';
     * MDFCN_DT IS '수정일시';
     */
}
