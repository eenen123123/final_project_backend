package kr.or.ddit.finalProject.dto.exam;

import lombok.Data;

import java.util.List;

/**
 * 시험 저장 요청 DTO (등록 / 수정 공용)
 *
 * POST /instructor/exams/create 또는
 * POST /instructor/exams/{examSn}/update 폼 제출 시 바인딩됩니다.
 *
 * [문항 선택]
 *   qstnSnList: 체크박스로 선택한 문항의 QSTN_SN 목록
 *   순서는 목록에서 선택된 순서(인덱스 + 1)를 QSTN_ORDR로 사용합니다.
 *
 * [날짜 형식]
 *   HTML datetime-local 입력값: "YYYY-MM-DDTHH:mm" 형식
 *   Oracle INSERT 시 TO_TIMESTAMP 변환 후 저장합니다.
 */
@Data
public class ExamSaveRequest {

    /** 시험명 (필수) */
    private String examRegNm;

    /** 연결할 강좌(클래스) SN (CLASSROOM.CLASS_SN, nullable) */
    private Long classSn;

    /** 시험 유형 코드 (COM_CD 참조, 선택 입력) */
    private String examTypeCd;

    /**
     * 시험 시작 일시 (datetime-local 입력값, 형식: "YYYY-MM-DDTHH:mm")
     * 선택 입력, null 허용
     */
    private String examStrtDt;

    /**
     * 시험 종료 일시 (datetime-local 입력값, 형식: "YYYY-MM-DDTHH:mm")
     * 선택 입력, null 허용
     */
    private String examEndDt;

    /**
     * 시험에 포함할 문항의 QSTN_SN 목록
     * 체크박스 name="qstnSnList"로 복수 선택, Spring이 List<Long>으로 바인딩
     * 선택 순서가 EXAM_QUESTION.QSTN_ORDR에 반영됩니다.
     */
    private List<Long> qstnSnList;
}
