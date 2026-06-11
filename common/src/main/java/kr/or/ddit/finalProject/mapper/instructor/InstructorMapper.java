package kr.or.ddit.finalProject.mapper.instructor;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.instructor.InstructorDetailResponse;
import kr.or.ddit.finalProject.dto.instructor.InstructorListResponse;

@Mapper
public interface InstructorMapper {

    /**
     * 강사 조회
     *
     * @param subjClId
     * @return
     */
    List<InstructorListResponse> selectInstructors(@Param("subjClId") Long subjClId);

    InstructorDetailResponse selectInstructorByUuid(@Param("instrUuid") String instrUuid);

}
