package kr.or.ddit.finalProject.mapper.classroom;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.classroom.ClassroomMemberListResponse;

@Mapper
public interface ClassroomMemberMapper {

    List<ClassroomMemberListResponse> selectMembersByClassSn(@Param("classSn") Long classSn);

}
