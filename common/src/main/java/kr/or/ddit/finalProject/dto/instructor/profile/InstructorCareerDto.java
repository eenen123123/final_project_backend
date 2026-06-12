package kr.or.ddit.finalProject.dto.instructor.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 강사 약력/저서/수상/방송출연 항목 DTO
 *
 * INSTRUCTOR_CAREER 테이블과 1:1 매핑됩니다. 강사 개인 페이지
 * 관리(/instructor/profile/instructor)에서 프로필 공개 페이지에 노출될 이력 항목을 관리합니다.
 *
 * [CAREER_TYPE_CD 코드 정의] 01 - 약력 (기간: CAREER_STRT_YR ~ CAREER_END_YR) 02 - 저서
 * (연도: CAREER_STRT_YR만 사용) 03 - 수상 (연도: CAREER_STRT_YR만 사용) 04 - 방송출연 (연도:
 * CAREER_STRT_YR만 사용)
 *
 * [연도 컬럼 규칙] - 약력(01): CAREER_STRT_YR ~ CAREER_END_YR 기간으로 표현 - 나머지 :
 * CAREER_STRT_YR에 해당 연도 입력, CAREER_END_YR은 null - CAREER_END_YR = null → 화면에서
 * "현재"로 표시
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstructorCareerDto {

    /**
     * 약력 항목 일련번호 (PK, 시퀀스 SEQ_INSTRUCTOR_CAREER)
     */
    private Long careerSn;

    /**
     * 강사 ID (FK → INSTRUCTOR.INSTR_USER_ID)
     */
    private String instrUserId;

    /**
     * 약력 유형 코드 01: 약력 / 02: 저서 / 03: 수상 / 04: 방송출연
     */
    private String careerTypeCd;

    /**
     * 시작 연도 (4자리 문자열, 예: "2019") 약력(01)은 기간 시작 연도, 나머지 유형은 해당 연도
     */
    private String careerStrtYr;

    /**
     * 종료 연도 (4자리 문자열, nullable) null이면 화면에서 "현재"로 표시 약력(01) 유형에서만 사용하며, 나머지 유형은
     * 항상 null
     */
    private String careerEndYr;

    /**
     * 항목 내용 (예: "OO대학교 수학과 졸업", "파이썬 완전정복 출간")
     */
    private String careerCont;

    /**
     * 화면 표시 순서 (오름차순 정렬) 동일 유형 내 순서를 제어합니다.
     */
    private Integer sortOrd;

    // ── 이력 컬럼 (프로젝트 표준 패턴) ──────────────
    /**
     * 최초 등록자 ID
     */
    private String rgtrId;

    /**
     * 최초 등록일시
     */
    private String regDt;

    /**
     * 최종 수정자 ID
     */
    private String lastMdfrId;

    /**
     * 최종 수정일시
     */
    private String mdfcnDt;

    // ── 소프트 딜리트 컬럼 ───────────────────────────
    /**
     * 삭제 여부 (Y: 삭제 / N: 정상)
     */
    private String delYn;

    /**
     * 삭제 처리 일시
     */
    private String delDt;

    /**
     * 삭제 처리자 ID
     */
    private String delUserId;
}
