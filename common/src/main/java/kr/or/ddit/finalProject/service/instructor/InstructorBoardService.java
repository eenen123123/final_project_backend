package kr.or.ddit.finalProject.service.instructor;

import java.util.List;

import kr.or.ddit.finalProject.dto.instructor.InstructorBoardDto;
import kr.or.ddit.finalProject.dto.instructor.InstructorBoardResponse;

public interface InstructorBoardService {

    /**
     * 강사 게시판 목록 조회
     */
    List<InstructorBoardResponse> getInstructorBoardList(String instrUserId);

    /**
     * 강사 게시판 상세 조회
     */
    InstructorBoardResponse getInstructorBoardDetail(Long postSn, String instrUserId);

    /**
     * 강사 게시판 등록
     */
    int insertInstructorBoard(InstructorBoardDto instructorBoardDto);

    /**
     * 강사 게시판 수정
     */
    int updateInstructorBoard(InstructorBoardDto instructorBoardDto);

    /**
     * 강사 게시판 삭제 (소프트)
     */
    int deleteInstructorBoard(Long postSn, String instrUserId);

    /**
     * 강사 게시판 복구
     */
    int restoreInstructorBoard(Long postSn, String instrUserId);
}
