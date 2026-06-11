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

    // 파일 ID로 강의 조회 -> 강의로 강좌 조회 -> 강좌로 클래스룸 조회 -> 클래스룸에 학생이 포함되어있는지?
    int existsByFileIdAndUserId(@Param("fileId") Long fileId, @Param("userId") String userId);
}
