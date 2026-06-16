package kr.or.ddit.finalProject.service.instructor;

import java.util.List;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardDto;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardResponse;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorPublicBoardDetail;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorPublicBoardItem;

public interface InstructorBoardService {

    /**
     * 강사 게시판 목록 조회 (검색 + 페이징)
     */
    PageResponse<InstructorBoardResponse> getInstructorBoardList(
            String instrUserId, String keyword, String boardTypeCd, String source, int page, int pageSize);

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

    int getUnansweredQnaCount(Long classSn);

    // ── 공개 강사 게시판 Q&A 답변 ──────────────────────────────────

    int answerInstructorQna(Long postSn, String answrUserId, String answCn);

    // ── 공개 강사 게시판 (React 프론트용) ──────────────────────────

    /** 강사 공개 게시판 목록을 페이징하여 반환한다. boardTypeCd: 02=공지, 03=QnA, 04=자료실 */
    PageResponse<InstructorPublicBoardItem> getPublicBoardList(String instrUuid, String boardTypeCd, int page, int size);

    /** 강사 공개 게시판 상세 정보(이전/다음글, 첨부파일 포함)를 반환하고 조회수를 증가시킨다. */
    InstructorPublicBoardDetail getPublicBoardDetail(String instrUuid, Long postSn);
}
