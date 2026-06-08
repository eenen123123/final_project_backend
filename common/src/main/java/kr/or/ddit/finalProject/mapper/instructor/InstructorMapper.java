package kr.or.ddit.finalProject.mapper.instructor;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import kr.or.ddit.finalProject.dto.instructor.InstructorListResponse;

@Mapper
public interface InstructorMapper {

    List<InstructorListResponse> selectInstructorsBySubject();

}
