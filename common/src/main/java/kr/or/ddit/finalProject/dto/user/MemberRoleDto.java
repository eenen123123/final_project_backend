package kr.or.ddit.finalProject.dto.user;

import lombok.Data;

@Data
public class MemberRoleDto {
    private Integer authrtSn; // 권한일련번호 (PK, AUTO INCREMENT)
    private String authrtCd; // 권한코드 (ADMIN/USER/GUEST 등)
    private String authrtNm; // 권한명
    private String mdfrId; // 수정자아이디
    private String mdfcnDt; // 수정일시
    private String frstRegDt; // 최초등록일시
    private String frstRgtrId; // 최초등록자아이디
    private String lastMdfrId; // 최종수정자아이디
    private String authrtExpln; // 권한설명
    private String useYn; // 사용여부 (Y:사용, N:미사용)
    private String lastMdfcnDt; // 최종수정일시 (이력 추적용)

    /*
     * COMMENT ON TABLE MEMBER_ROLE IS '권한 관리';
     * 
     * AUTHRT_SN IS '권한일련번호 (PK, AUTO INCREMENT)';
     * AUTHRT_CD IS '권한코드 (ADMIN/USER/GUEST 등)';
     * AUTHRT_NM IS '권한명';
     * MDFR_ID IS '수정자아이디';
     * MDFCN_DT IS '수정일시';
     * FRST_REG_DT IS '최초등록일시';
     * FRST_RGTR_ID IS '최초등록자아이디';
     * LAST_MDFR_ID IS '최종수정자아이디';
     * AUTHRT_EXPLN IS '권한설명';
     * USE_YN IS '사용여부 (Y:사용, N:미사용)';
     * LAST_MDFCN_DT IS '최종수정일시 (이력 추적용)';
     */
}
