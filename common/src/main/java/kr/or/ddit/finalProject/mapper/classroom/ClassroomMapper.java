package kr.or.ddit.finalProject.mapper.classroom;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.classroom.ClassroomDto;
import kr.or.ddit.finalProject.dto.classroom.ClassroomMemberDto;

@Mapper
public interface ClassroomMapper {

    ClassroomDto selectClassroomById(@Param("classId") Long classId);

    List<ClassroomMemberDto> selectMembersByClassId(@Param("classId") Long classId, @Param("enrlStatCd") String enrlStatCd);
}
