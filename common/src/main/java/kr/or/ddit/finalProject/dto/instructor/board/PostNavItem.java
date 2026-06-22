package kr.or.ddit.finalProject.dto.instructor.board;

import lombok.Data;

/**
 * 게시글 이전/다음 네비게이션 항목 DTO.
 * selectPrevPost / selectNextPost 쿼리 결과를 담으며,
 * InstructorPublicBoardDetail.prevPost / nextPost 필드로 포함된다.
 */
@Data
public class PostNavItem {

    /** 이전 또는 다음 게시글 일련번호 */
    private Long postSn;

    /** 이전 또는 다음 게시글 제목 */
    private String title;
}
