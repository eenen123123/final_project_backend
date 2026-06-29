package kr.or.ddit.finalProject.mapper.instructor;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardDto;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardFileItem;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorPublicBoardDetail;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorPublicBoardItem;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorQnaAnswerDto;
import kr.or.ddit.finalProject.dto.instructor.profile.InstructorRecentPostResponse;
import kr.or.ddit.finalProject.dto.instructor.board.PostNavItem;

/**
 * 강사 게시판 Mapper.
 * INSTRUCTOR_BOARD 테이블과 INSTRUCTOR_QNA 테이블에 대한 CRUD를 담당한다.
 * CLASS_SN IS NULL → 강사 홈페이지 게시판 / CLASS_SN 값 있음 → 클래스룸 전속 게시판
 */
@Mapper
public interface InstructorBoardMapper {

    // ── 강사 홈페이지 게시판 ──────────────────────────────────────────

    /**
     * 강사 홈페이지 게시판 목록 조회 (페이징, 검색 필터 포함).
     * searchType: "" 또는 null=전체, "title"=제목, "content"=내용,
     *             "titleContent"=제목+내용, "writer"=작성자
     */
    List<InstructorBoardDto> selectInstructorBoardList(
            @Param("instrUserId") String instrUserId,
            @Param("keyword") String keyword,
            @Param("boardTypeCd") String boardTypeCd,
            @Param("searchType") String searchType,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);

    /** 강사 홈페이지 게시판 총 건수 (목록과 동일한 검색 조건 적용) */
    int selectInstructorBoardCount(
            @Param("instrUserId") String instrUserId,
            @Param("keyword") String keyword,
            @Param("boardTypeCd") String boardTypeCd,
            @Param("searchType") String searchType);

    /** 강사 홈페이지 게시글 상세 조회 (본문 CLOB 포함) */
    InstructorBoardDto selectInstructorBoardDetail(
            @Param("postSn") Long postSn,
            @Param("instrUserId") String instrUserId);

    /**
     * 강사 홈페이지 게시글 등록.
     * useGeneratedKeys=true로 POST_SN이 dto.postSn에 자동 채워진다.
     */
    int insertInstructorBoard(InstructorBoardDto instructorBoardDto);

    /** 강사 홈페이지 게시글 수정 (제목, 본문, 분류, 첨부파일 그룹 ID) */
    int updateInstructorBoard(InstructorBoardDto instructorBoardDto);

    /** 강사 홈페이지 게시글 소프트 삭제 (USE_YN = 'N') */
    int deleteInstructorBoard(
            @Param("postSn") Long postSn,
            @Param("instrUserId") String instrUserId);

    /** 강사 홈페이지 게시글 하드 삭제 (파일 업로드 실패 보상용, 실제 행 제거) */
    int hardDeleteInstructorBoard(@Param("postSn") Long postSn);

    /** 강사 홈페이지 게시글 복구 (USE_YN = 'Y') */
    int restoreInstructorBoard(
            @Param("postSn") Long postSn,
            @Param("instrUserId") String instrUserId);

    // ── 강사 홈페이지 Q&A 답변 ───────────────────────────────────────

    /** Q&A 답변 정보 조회 (INSTRUCTOR_QNA + MEMBER 조인) */
    InstructorQnaAnswerDto selectInstructorQnaAnswer(@Param("postSn") Long postSn);

    /** Q&A child 레코드 생성 (게시글 등록 시 boardTypeCd='QNA' 이면 자동 호출) */
    int insertInstructorQna(@Param("postSn") Long postSn);

    /** Q&A 답변 등록 또는 수정 (ANSW_CN, ANSWR_USER_ID, ANSW_DT, ANSW_YN='Y' 갱신) */
    int updateInstructorQnaAnswer(
            @Param("postSn") Long postSn,
            @Param("answrUserId") String answrUserId,
            @Param("answCn") String answCn);

    // ── 클래스룸 공지사항 ──────────────────────────────────────────────

    /** 클래스룸 공지사항 목록 조회 (페이징) */
    List<InstructorBoardDto> selectClassroomNoticeList(
            @Param("classSn") Long classSn,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);

    /** 클래스룸 공지사항 총 건수 */
    int countClassroomNoticeList(@Param("classSn") Long classSn);

    /** 클래스룸 공지사항 상세 조회 */
    InstructorBoardDto selectClassroomNoticeDetail(
            @Param("postSn") Long postSn,
            @Param("classSn") Long classSn);

    /** 클래스룸 공지사항 등록 */
    int insertClassroomNotice(InstructorBoardDto dto);

    /** 클래스룸 공지사항 수정 */
    int updateClassroomNotice(InstructorBoardDto dto);

    /** 클래스룸 공지사항 소프트 삭제 */
    int deleteClassroomNotice(
            @Param("postSn") Long postSn,
            @Param("classSn") Long classSn);

    // ── 클래스룸 자료실 ───────────────────────────────────────────────

    /** 클래스룸 자료실 목록 조회 (페이징) */
    List<InstructorBoardDto> selectClassroomDataroomList(
            @Param("classSn") Long classSn,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);

    /** 클래스룸 자료실 총 건수 */
    int countClassroomDataroomList(@Param("classSn") Long classSn);

    /** 클래스룸 자료실 상세 조회 */
    InstructorBoardDto selectClassroomDataroomDetail(
            @Param("postSn") Long postSn,
            @Param("classSn") Long classSn);

    /** 클래스룸 자료실 등록 */
    int insertClassroomDataroom(InstructorBoardDto dto);

    /** 클래스룸 자료실 수정 */
    int updateClassroomDataroom(InstructorBoardDto dto);

    /** 클래스룸 자료실 소프트 삭제 */
    int deleteClassroomDataroom(
            @Param("postSn") Long postSn,
            @Param("classSn") Long classSn);

    // ── 클래스룸 Q&A ──────────────────────────────────────────────────

    /** 클래스룸 Q&A 목록 조회 (페이징, writerUserId null이면 전체) */
    List<kr.or.ddit.finalProject.dto.classroom.ClassroomQnaDto> selectClassroomQnaList(
            @Param("classSn") Long classSn,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize,
            @Param("writerUserId") String writerUserId);

    /** 클래스룸 Q&A 총 건수 (writerUserId null이면 전체) */
    int countClassroomQnaList(@Param("classSn") Long classSn, @Param("writerUserId") String writerUserId);

    /** 클래스룸 Q&A 상세 조회 (답변 정보 포함) */
    kr.or.ddit.finalProject.dto.classroom.ClassroomQnaDto selectClassroomQnaDetail(
            @Param("postSn") Long postSn,
            @Param("classSn") Long classSn);

    /** 클래스룸 Q&A 게시글 본문 등록 (INSTRUCTOR_BOARD INSERT) */
    int insertClassroomQnaBoard(InstructorBoardDto dto);

    /** 클래스룸 Q&A child 레코드 생성 (INSTRUCTOR_QNA INSERT, insertClassroomQnaBoard 직후 호출) */
    int insertClassroomQnaChild(@Param("postSn") Long postSn);

    /** 클래스룸 Q&A 본문 수정 (제목·내용·첨부파일) */
    int updateClassroomQna(
            @Param("postSn") Long postSn,
            @Param("classSn") Long classSn,
            @Param("postSj") String postSj,
            @Param("postCn") String postCn,
            @Param("atchFileId") Long atchFileId,
            @Param("updateAtchFile") boolean updateAtchFile);

    /** 클래스룸 Q&A 답변 등록 또는 수정 */
    int updateClassroomQnaAnswer(
            @Param("postSn") Long postSn,
            @Param("answrUserId") String answrUserId,
            @Param("answCn") String answCn);

    /** 클래스룸 내 미답변 Q&A 건수 조회 (대시보드 배지 표시용) */
    int selectUnansweredQnaCount(@Param("classSn") Long classSn);

    /** 강사 게시판 미답변 Q&A 총 건수 (강사 대시보드용) */
    int countUnansweredInstructorQna(@Param("userId") String userId);

    /** 강사 게시판 최근 미답변 Q&A 목록 (강사 대시보드 목록 섹션용) */
    List<kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardDto> selectRecentUnansweredInstructorQna(
            @Param("userId") String userId,
            @Param("limit") int limit);

    /** 특정 학생이 등록한 최근 QnA 조회 (학생 상세 페이지용) */
    List<kr.or.ddit.finalProject.dto.classroom.ClassroomQnaDto> selectRecentQnaByStudent(
            @Param("classSn") Long classSn,
            @Param("userId") String userId,
            @Param("limit") int limit);

    // ── 강사 프로필 ───────────────────────────────────────────────────

    /** 강사 홈페이지 최근 게시글 조회 (프로필 페이지 미리보기용, size건 제한) */
    List<InstructorRecentPostResponse> selectRecentPosts(
            @Param("instrUuid") String instrUuid,
            @Param("size") int size);

    // ── 공개 강사 게시판 (React 프론트 전용) ─────────────────────────

    /** 공개 게시판 총 건수 (QNA는 비밀글 제외) */
    int selectPublicBoardCount(
            @Param("instrUuid") String instrUuid,
            @Param("boardTypeCd") String boardTypeCd);

    /** 공개 게시판 목록 조회 (페이징, QNA는 비밀글 제외) */
    List<InstructorPublicBoardItem> selectPublicBoardList(
            @Param("instrUuid") String instrUuid,
            @Param("boardTypeCd") String boardTypeCd,
            @Param("offset") int offset,
            @Param("limit") int limit);

    /** 공개 게시판 상세 조회 (이전/다음글, 답변 정보 포함) */
    InstructorPublicBoardDetail selectPublicBoardDetail(
            @Param("instrUuid") String instrUuid,
            @Param("postSn") Long postSn);

    /**
     * 조회수 증가.
     * 수강생이 공개 게시판 상세를 열람할 때만 호출한다.
     * 강사 관리 화면(클래스룸, 관리자 페이지)에서는 호출하지 않는다.
     */
    int incrementViewCount(@Param("postSn") Long postSn);

    /** 같은 분류 내 이전 게시글 (postSn 기준 바로 아래) */
    PostNavItem selectPrevPost(
            @Param("instrUuid") String instrUuid,
            @Param("boardTypeCd") String boardTypeCd,
            @Param("postSn") Long postSn);

    /** 같은 분류 내 다음 게시글 (postSn 기준 바로 위) */
    PostNavItem selectNextPost(
            @Param("instrUuid") String instrUuid,
            @Param("boardTypeCd") String boardTypeCd,
            @Param("postSn") Long postSn);

    /** 게시글 첨부파일 목록 조회 (DEL_YN='N' 인 파일만) */
    List<InstructorBoardFileItem> selectBoardFiles(@Param("postSn") Long postSn);

    // ── 나의 선생님 Q&A (마이페이지) ─────────────────────────────────

    /** 내가 작성한 강사 홈페이지 Q&A 목록 (페이징) */
    List<Map<String, Object>> selectMyInstructorQnaList(
            @Param("userId") String userId,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);

    /** 내가 작성한 강사 홈페이지 Q&A 총 건수 */
    int countMyInstructorQnaList(@Param("userId") String userId);
}
