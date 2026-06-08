package kr.or.ddit.finalProject.dto.instructor;

import lombok.Data;

/**
 * 강사 소개글 수정 요청 DTO
 *
 * POST /instructor/profile/instructor/intro 폼 제출 시 바인딩됩니다.
 * INSTRUCTOR.INSTR_INTRO 컬럼 하나만 수정합니다.
 */
@Data
public class InstructorIntroUpdateRequest {

    /**
     * 강사 소개글 (INSTRUCTOR.INSTR_INTRO)
     */
    private String instrIntro;
}
