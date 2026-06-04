package kr.or.ddit.finalProject.dto.instructor;

import lombok.Data;

/**
 * 약력 항목 단건 저장 요청 DTO
 *
 * POST /instructor/profile/teacher/careers 폼 제출 시 바인딩됩니다.
 * 저장 전 instrUserId는 컨트롤러에서 로그인 사용자 ID로 주입합니다.
 *
 * [유형별 연도 입력 규칙]
 *   - 약력(01) : careerStrtYr + careerEndYr (기간, endYr 비워두면 "현재")
 *   - 저서/수상/방송출연(02~04) : careerStrtYr만 입력, careerEndYr 무시
 */
@Data
public class InstructorCareerSaveRequest {

    /**
     * 약력 유형 코드
     * 01: 약력 / 02: 저서 / 03: 수상 / 04: 방송출연
     */
    private String careerTypeCd;

    /** 시작 연도 (4자리, 예: "2020") */
    private String careerStrtYr;

    /**
     * 종료 연도 (4자리, nullable)
     * 비어있으면 null로 처리 → 화면에서 "현재"로 표시
     */
    private String careerEndYr;

    /** 항목 내용 */
    private String careerCont;

    /** 화면 표시 순서 */
    private Integer sortOrd;
}
