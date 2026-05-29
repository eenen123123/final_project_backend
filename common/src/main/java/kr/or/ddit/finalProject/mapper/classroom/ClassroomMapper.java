package kr.or.ddit.finalProject.mapper.classroom;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse;
import kr.or.ddit.finalProject.dto.classroom.ClassroomListResponse;

@Mapper
public interface ClassroomMapper {

    List<ClassroomListResponse> selectClassroomListByInstructor(@Param("instrUserId") String instrUserId);

    ClassroomDetailResponse selectClassroomBySn(@Param("classSn") Long classSn);

}
