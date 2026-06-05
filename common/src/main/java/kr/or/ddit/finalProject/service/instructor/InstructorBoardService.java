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

    // ── 클래스룸 공지사항 ──────────────────────────────────────────

    List<InstructorBoardDto> getClassroomNoticeList(Long classSn);

    InstructorBoardDto getClassroomNoticeDetail(Long postSn, Long classSn);

    int insertClassroomNotice(InstructorBoardDto dto);

    int deleteClassroomNotice(Long postSn, Long classSn);

    // ── 클래스룸 Q&A ──────────────────────────────────────────────

    List<kr.or.ddit.finalProject.dto.classroom.ClassroomQnaDto> getClassroomQnaList(Long classSn);

    kr.or.ddit.finalProject.dto.classroom.ClassroomQnaDto getClassroomQnaDetail(Long postSn, Long classSn);

    void insertClassroomQna(InstructorBoardDto dto);

    void answerClassroomQna(Long postSn, String answrUserId, String answCn);
}
