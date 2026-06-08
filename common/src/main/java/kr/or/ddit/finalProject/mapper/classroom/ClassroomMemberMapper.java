package kr.or.ddit.finalProject.mapper.classroom;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.classroom.ClassroomGradeDto;
import kr.or.ddit.finalProject.dto.classroom.ClassroomListResponse;
import kr.or.ddit.finalProject.dto.classroom.ClassroomMemberListResponse;

@Mapper
public interface ClassroomMemberMapper {

    List<ClassroomMemberListResponse> selectMembersByClassSn(@Param("classSn") Long classSn);

    List<ClassroomGradeDto> selectGradeList(@Param("classSn") Long classSn);

    List<ClassroomListResponse> selectClassroomsByUserId(@Param("userId") String userId);

}
