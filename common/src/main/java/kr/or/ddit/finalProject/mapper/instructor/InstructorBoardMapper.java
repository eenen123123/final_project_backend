package kr.or.ddit.finalProject.mapper.instructor;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.instructor.InstructorBoardDto;

@Mapper
public interface InstructorBoardMapper {

    public List<InstructorBoardDto> selectInstructorBoardList(@Param("instrUserId") String instrUserId);

    public InstructorBoardDto selectInstructorBoardDetail(@Param("postSn") Long postSn, @Param("instrUserId") String instrUserId);

    public int insertInstructorBoard(InstructorBoardDto instructorBoardDto);

    public int updateInstructorBoard(InstructorBoardDto instructorBoardDto);

    public int deleteInstructorBoard(@Param("postSn") Long postSn, @Param("instrUserId") String instrUserId);

    public int restoreInstructorBoard(@Param("postSn") Long postSn, @Param("instrUserId") String instrUserId);
}
