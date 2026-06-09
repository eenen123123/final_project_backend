package kr.or.ddit.finalProject.mapper.instructor;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.instructor.InstructorBoardDto;
import kr.or.ddit.finalProject.dto.instructor.InstructorRecentPostResponse;

@Mapper
public interface InstructorBoardMapper {

    public List<InstructorBoardDto> selectInstructorBoardList(@Param("instrUserId") String instrUserId);

    public InstructorBoardDto selectInstructorBoardDetail(@Param("postSn") Long postSn, @Param("instrUserId") String instrUserId);

    public int insertInstructorBoard(InstructorBoardDto instructorBoardDto);

    public int updateInstructorBoard(InstructorBoardDto instructorBoardDto);

    public int deleteInstructorBoard(@Param("postSn") Long postSn, @Param("instrUserId") String instrUserId);

    public int restoreInstructorBoard(@Param("postSn") Long postSn, @Param("instrUserId") String instrUserId);

    // ── 클래스룸 공지사항 ──────────────────────────────────────────

    List<InstructorBoardDto> selectClassroomNoticeList(@Param("classSn") Long classSn);

    InstructorBoardDto selectClassroomNoticeDetail(@Param("postSn") Long postSn, @Param("classSn") Long classSn);

    int insertClassroomNotice(InstructorBoardDto dto);

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
}
