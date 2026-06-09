package kr.or.ddit.finalProject.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import kr.or.ddit.finalProject.dto.student.StudentDto;
import kr.or.ddit.finalProject.dto.user.ParentJoinLinkDto;

@Mapper
public interface ParentJoinMapper {

    int insertParentJoinLink(ParentJoinLinkDto joinLinkDto);

    ParentJoinLinkDto findByJoinLinkAddr(String joinLinkAddr);

    List<ParentJoinLinkDto> findByStudentId(String studentId);

    int deleteParentJoinLink(Long linkId);

    // Student 테이블에 학생과 학부모의 관계를 설정
    int insertParentChildRelation(StudentDto studentDto);
}
