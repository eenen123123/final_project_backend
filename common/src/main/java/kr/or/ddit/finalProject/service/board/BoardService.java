package kr.or.ddit.finalProject.service.board;

import kr.or.ddit.finalProject.dto.board.BoardDto;

public interface BoardService {

    /**
     * 게시글 등록
     *
     * @param boardDto 등록할 게시글 정보
     */
    void createPost(BoardDto boardDto);

    /**
     * 게시글 수정
     *
     * @param boardDto 수정할 게시글 정보
     */
    void updatePost(BoardDto boardDto);

    /**
     * 게시글 삭제
     *
     * @param postSn 삭제할 게시글 PK
     */
    void deletePost(Long postSn);

}
