package kr.or.ddit.finalProject.mapper.board;

import org.apache.ibatis.annotations.Mapper;

import kr.or.ddit.finalProject.dto.board.BoardDto;

@Mapper
public interface BoardMapper {

    // 게시글 단건 조회
    BoardDto selectBoardByPostSn(Long postSn);

    // 게시글 등록
    int insertBoard(BoardDto boardDto);

    // 게시글 수정
    int updateBoard(BoardDto boardDto);

    // 게시글 삭제
    int deleteBoard(Long postSn);
}
