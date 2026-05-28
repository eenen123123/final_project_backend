package kr.or.ddit.finalProject.service.instructor;

import java.util.List;

import kr.or.ddit.finalProject.dto.instructor.InstructorBoardDto;
import kr.or.ddit.finalProject.responseDto.instructor.InstructorBoardResponseDto;

public interface InstructorBoardService {

    /**
     * 강사 게시판 목록 조회
     */
    List<InstructorBoardResponseDto> getInstructorBoardList(String instrUserId);

    /**
     * 강사 게시판 상세 조회
     */
    InstructorBoardResponseDto getInstructorBoardDetail(int postSn);

    /**
     * 강사 게시판 등록
     */
    int insertInstructorBoard(InstructorBoardDto instructorBoardDto);

    /**
     * 강사 게시판 수정
     */
    int updateInstructorBoard(InstructorBoardDto instructorBoardDto);

    /**
     * 강사 게시판 삭제
     */
    int deleteInstructorBoard(int postSn);
}
