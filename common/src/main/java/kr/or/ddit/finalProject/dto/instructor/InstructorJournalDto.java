package kr.or.ddit.finalProject.dto.instructor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 강사 업무 일지 DTO
 *
 * INSTRUCTOR_JOURNAL 테이블과 1:1 매핑됩니다.
 * 목록·상세 조회 시 MEMBER.USER_NAME(instrUserNm) JOIN 결과도 포함합니다.
 * 업무 일지는 특정 클래스에 종속되지 않으므로 클래스 관련 필드가 없습니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstructorJournalDto {

    /** 일지 일련번호 (PK) */
    private Long jrnlSn;

    /** 작성 강사 ID (FK → MEMBER.USER_ID) */
    private String instrUserId;

    /** 작성 강사 이름 — MEMBER.USER_NAME JOIN (목록/상세 표시용) */
    private String instrUserNm;

    /** 일지 제목 */
    private String jrnlTitle;

    /** 일지 본문 */
    private String jrnlCont;

    /**
     * 업무 실시 날짜 (yyyy-MM-dd 문자열)
     * Oracle DATE 타입을 TO_CHAR로 변환하여 받습니다.
     * HTML input[type=date] 와 포맷이 일치해 별도 변환이 불필요합니다.
     */
    private String jrnlDt;

    /** 등록일시 (표시용) */
    private String regDt;

    /** 수정일시 (표시용, nullable) */
    private String mdfcnDt;
}
