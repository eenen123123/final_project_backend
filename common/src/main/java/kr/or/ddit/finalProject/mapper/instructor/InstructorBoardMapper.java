package kr.or.ddit.finalProject.mapper.instructor;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardDto;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardFileItem;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorPublicBoardDetail;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorPublicBoardItem;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorQnaAnswerDto;
import kr.or.ddit.finalProject.dto.instructor.profile.InstructorRecentPostResponse;
import kr.or.ddit.finalProject.dto.instructor.board.PostNavItem;

@Mapper
public interface InstructorBoardMapper {

    public List<InstructorBoardDto> selectInstructorBoardList(
            @Param("instrUserId") String instrUserId,
            @Param("keyword") String keyword,
            @Param("boardTypeCd") String boardTypeCd,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);

    public int selectInstructorBoardCount(
            @Param("instrUserId") String instrUserId,
            @Param("keyword") String keyword,
            @Param("boardTypeCd") String boardTypeCd);

    public InstructorBoardDto selectInstructorBoardDetail(@Param("postSn") Long postSn, @Param("instrUserId") String instrUserId);

    public int insertInstructorBoard(InstructorBoardDto instructorBoardDto);

    public int updateInstructorBoard(InstructorBoardDto instructorBoardDto);

    public int deleteInstructorBoard(@Param("postSn") Long postSn, @Param("instrUserId") String instrUserId);

    public int restoreInstructorBoard(@Param("postSn") Long postSn, @Param("instrUserId") String instrUserId);

    // ── 클래스룸 공지사항 ──────────────────────────────────────────

    List<InstructorBoardDto> selectClassroomNoticeList(@Param("classSn") Long classSn);

    InstructorBoardDto selectClassroomNoticeDetail(@Param("postSn") Long postSn, @Param("classSn") Long classSn);

    int insertClassroomNotice(InstructorBoardDto dto);

    int updateClassroomNotice(InstructorBoardDto dto);

    int deleteClassroomNotice(@Param("postSn") Long postSn, @Param("classSn") Long classSn);

    // ── 클래스룸 Q&A ──────────────────────────────────────────────

    List<kr.or.ddit.finalProject.dto.classroom.ClassroomQnaDto> selectClassroomQnaList(@Param("classSn") Long classSn);

    kr.or.ddit.finalProject.dto.classroom.ClassroomQnaDto selectClassroomQnaDetail(@Param("postSn") Long postSn, @Param("classSn") Long classSn);

    int insertClassroomQnaBoard(InstructorBoardDto dto);

    int insertClassroomQnaChild(@Param("postSn") Long postSn);

    int updateClassroomQnaAnswer(@Param("postSn") Long postSn,
                                 @Param("answrUserId") String answrUserId,
                                 @Param("answCn") String answCn);

    int selectUnansweredQnaCount(@Param("classSn") Long classSn);

    List<InstructorRecentPostResponse> selectRecentPosts(@Param("instrUuid") String instrUuid,
                                                         @Param("size") int size);

    // ── 공개 강사 게시판 Q&A 답변 ──────────────────────────────────

    InstructorQnaAnswerDto selectInstructorQnaAnswer(@Param("postSn") Long postSn);

    int insertInstructorQna(@Param("postSn") Long postSn);

    int updateInstructorQnaAnswer(@Param("postSn") Long postSn,
                                  @Param("answrUserId") String answrUserId,
                                  @Param("answCn") String answCn);

    int incrementViewCount(@Param("postSn") Long postSn);

    PostNavItem selectPrevPost(@Param("instrUuid") String instrUuid,
                               @Param("boardTypeCd") String boardTypeCd,
                               @Param("postSn") Long postSn);

    PostNavItem selectNextPost(@Param("instrUuid") String instrUuid,
                               @Param("boardTypeCd") String boardTypeCd,
                               @Param("postSn") Long postSn);

    List<InstructorBoardFileItem> selectBoardFiles(@Param("postSn") Long postSn);

    // ── 공개 강사 게시판 (React 프론트용 페이징 목록 + 상세) ──────────

    int selectPublicBoardCount(@Param("instrUuid") String instrUuid,
                               @Param("boardTypeCd") String boardTypeCd);

    List<InstructorPublicBoardItem> selectPublicBoardList(@Param("instrUuid") String instrUuid,
                                                          @Param("boardTypeCd") String boardTypeCd,
                                                          @Param("offset") int offset,
                                                          @Param("limit") int limit);

    InstructorPublicBoardDetail selectPublicBoardDetail(@Param("instrUuid") String instrUuid,
                                                        @Param("postSn") Long postSn);
}
