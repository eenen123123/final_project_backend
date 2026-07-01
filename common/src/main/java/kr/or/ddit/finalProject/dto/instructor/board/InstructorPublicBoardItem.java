package kr.or.ddit.finalProject.dto.instructor.board;

import lombok.Data;

/**
 * 강사 공개 게시판 목록 항목 DTO (React 프론트 전용).
 * selectPublicBoardList 쿼리 결과를 직접 담는다.
 * 본문(content)은 목록에서 불필요하므로 포함하지 않는다.
 */
@Data
public class InstructorPublicBoardItem {

    /** 게시글 일련번호 (PK) */
    private Long postSn;

    /** 게시글 제목 */
    private String title;

    /** 등록일 (yyyy-MM-dd) */
    private String regDt;

    /** 조회수 */
    private Integer viewCount;

    /** 작성자 이름 */
    private String writerName;

    /** Q&A 답변 완료 여부 (Y/N, QNA 타입일 때만 유효) */
    private String answerYn;

    /** 첨부파일 존재 여부 (Y/N) */
    private String hasFile;

    /** 비밀글 여부 (Y/N, QNA 타입일 때만 유효) */
    private String secrYn;
}
