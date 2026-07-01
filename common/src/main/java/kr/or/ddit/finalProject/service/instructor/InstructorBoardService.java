package kr.or.ddit.finalProject.service.instructor;

import java.util.List;

import kr.or.ddit.finalProject.dto.classroom.ClassroomQnaDto;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardDto;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardResponse;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorPublicBoardDetail;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorPublicBoardItem;

/**
 * 강사 게시판 서비스.
 * 강사 홈페이지 게시판, 클래스룸 공지/자료실/Q&A, React 프론트용 공개 게시판을 담당한다.
 */
public interface InstructorBoardService {

    // ── 강사 홈페이지 게시판 ──────────────────────────────────────────

    /**
     * 강사 홈페이지 게시판 목록 조회 (검색 + 페이징).
     * searchType: "" 또는 null=전체, "title"=제목, "content"=내용,
     *             "titleContent"=제목+내용, "writer"=작성자
     */
    PageResponse<InstructorBoardResponse> getInstructorBoardList(
            String instrUserId, String keyword, String boardTypeCd, String searchType,
            int page, int pageSize);

    /** 강사 홈페이지 게시글 상세 조회 (본문, Q&A 답변, 첨부파일 포함) */
    InstructorBoardResponse getInstructorBoardDetail(Long postSn, String instrUserId);

    /** 강사 홈페이지 게시글 등록 (QNA 타입이면 INSTRUCTOR_QNA child 레코드도 생성) */
    int insertInstructorBoard(InstructorBoardDto instructorBoardDto);

    /** 강사 홈페이지 게시글 수정 */
    int updateInstructorBoard(InstructorBoardDto instructorBoardDto);

    /** 강사 홈페이지 게시글 소프트 삭제 (USE_YN = 'N') */
    int deleteInstructorBoard(Long postSn, String instrUserId);

    /** 강사 홈페이지 게시글 하드 삭제 (파일 업로드 실패 보상용) */
    int hardDeleteInstructorBoard(Long postSn);

    /** 강사 홈페이지 게시글 복구 (USE_YN = 'Y') */
    int restoreInstructorBoard(Long postSn, String instrUserId);

    /** 강사 홈페이지 Q&A 답변 등록 또는 수정 */
    int answerInstructorQna(Long postSn, String answrUserId, String answCn);

    // ── 클래스룸 공지사항 ──────────────────────────────────────────────

    /** 클래스룸 공지사항 목록 조회 (페이징) */
    PageResponse<InstructorBoardDto> getClassroomNoticeList(Long classSn, int page, int pageSize);

    /** 클래스룸 공지사항 상세 조회 */
    InstructorBoardDto getClassroomNoticeDetail(Long postSn, Long classSn);

    /** 클래스룸 공지사항 등록 */
    int insertClassroomNotice(InstructorBoardDto dto);

    /** 클래스룸 공지사항 수정 */
    int updateClassroomNotice(InstructorBoardDto dto);

    /** 클래스룸 공지사항 소프트 삭제 */
    int deleteClassroomNotice(Long postSn, Long classSn);

    // ── 클래스룸 자료실 ───────────────────────────────────────────────

    /** 클래스룸 자료실 목록 조회 (페이징) */
    PageResponse<InstructorBoardDto> getClassroomDataroomList(Long classSn, int page, int pageSize);

    /** 클래스룸 자료실 상세 조회 */
    InstructorBoardDto getClassroomDataroomDetail(Long postSn, Long classSn);

    /** 클래스룸 자료실 등록 */
    int insertClassroomDataroom(InstructorBoardDto dto);

    /** 클래스룸 자료실 수정 */
    int updateClassroomDataroom(InstructorBoardDto dto);

    /** 클래스룸 자료실 소프트 삭제 */
    int deleteClassroomDataroom(Long postSn, Long classSn);

    // ── 클래스룸 Q&A ──────────────────────────────────────────────────

    /** 클래스룸 Q&A 목록 조회 (페이징, writerUserId null이면 전체) */
    PageResponse<ClassroomQnaDto> getClassroomQnaList(Long classSn, int page, int pageSize, String writerUserId);

    /** 클래스룸 Q&A 상세 조회 */
    ClassroomQnaDto getClassroomQnaDetail(Long postSn, Long classSn);

    /** 클래스룸 Q&A 등록 (INSTRUCTOR_BOARD + INSTRUCTOR_QNA child 레코드 동시 생성) */
    void insertClassroomQna(InstructorBoardDto dto);

    /** 클래스룸 Q&A 본문 수정 (작성자 본인만) */
    void updateClassroomQna(Long postSn, Long classSn, String wrtrUserId, String postSj, String postCn, Long atchFileId, boolean updateAtchFile);

    /** 클래스룸 Q&A 답변 등록 또는 수정 */
    void answerClassroomQna(Long postSn, String answrUserId, String answCn);

    /** 클래스룸 미답변 Q&A 건수 조회 (대시보드 배지 표시용) */
    int getUnansweredQnaCount(Long classSn);

    /** 특정 학생이 등록한 최근 QnA 조회 (학생 상세 페이지용) */
    List<kr.or.ddit.finalProject.dto.classroom.ClassroomQnaDto> getRecentQnaByStudent(Long classSn, String userId, int limit);

    // ── 공개 강사 게시판 (React 프론트 전용) ─────────────────────────

    /**
     * 강사 공개 게시판 목록 조회 (페이징 + 검색).
     * @param pageIndex  0-based 페이지 인덱스 (React 프론트 기준)
     * @param searchType "title" | "writer" | null (전체)
     * @param keyword    검색어, null 또는 빈 문자열이면 전체 조회
     */
    PageResponse<InstructorPublicBoardItem> getPublicBoardList(
            String instrUuid, String boardTypeCd, int pageIndex, int size,
            String searchType, String keyword);

    /** 강사 공개 게시판 상세 조회 (이전/다음글, 첨부파일 포함) + 조회수 증가 */
    default InstructorPublicBoardDetail getPublicBoardDetail(String instrUuid, Long postSn) {
        return getPublicBoardDetail(instrUuid, postSn, null);
    }

    /**
     * 강사 공개 게시판 상세 조회 (currentUserId가 있으면 본인 비밀글도 조회 가능).
     * isMyPost·secrYn 필드는 이 메서드에서 채워진다.
     */
    InstructorPublicBoardDetail getPublicBoardDetail(String instrUuid, Long postSn, String currentUserId);

    /** 게시글 존재 여부 확인 (비밀글 여부 무관 — 403 vs 404 구분용) */
    boolean existsPublicBoard(String instrUuid, Long postSn);  // impl에서 COUNT > 0 변환

    /** 조회수 증가 — 학생이 게시글 상세를 열람할 때만 호출. 관리자 화면에서는 호출 금지 */
    void incrementViewCount(Long postSn);

    // ── 공개 강사 게시판 Q&A CRUD ─────────────────────────────────────

    /** 강사 공개 게시판 Q&A 등록 (INSTRUCTOR_BOARD + INSTRUCTOR_QNA child 동시 생성) */
    void insertPublicQna(String instrUuid, String wrtrUserId, String postSj, String postCn, String secrYn);

    /** 강사 공개 게시판 Q&A 수정 (작성자 본인만) */
    void updatePublicQna(Long postSn, String wrtrUserId, String postSj, String postCn, String secrYn);

    /** 강사 공개 게시판 Q&A 소프트 삭제 (작성자 본인만) */
    void deletePublicQna(Long postSn, String wrtrUserId);
}
