package kr.or.ddit.finalProject.dto.instructor.board;

import lombok.Data;

/**
 * 강사 게시판 Q&A 답변 정보 DTO.
 * INSTRUCTOR_QNA 테이블과 MEMBER 조인 결과를 담으며,
 * InstructorBoardResponse.answer 필드로 포함된다.
 */
@Data
public class InstructorQnaAnswerDto {

    /** 답변 완료 여부 (Y: 답변 완료, N: 미답변) */
    private String answYn;

    /** 답변 본문 */
    private String answCn;

    /** 답변 작성자 이름 (MEMBER.USER_NAME) */
    private String answrUserNm;

    /** 답변 등록일시 문자열 (yyyy-MM-dd HH:mm) */
    private String answDt;
}
