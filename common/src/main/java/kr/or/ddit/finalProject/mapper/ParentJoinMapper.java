package kr.or.ddit.finalProject.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import kr.or.ddit.finalProject.dto.user.ParentJoinLinkDto;

@Mapper
public interface ParentJoinMapper {

    int insertParentJoinLink(ParentJoinLinkDto joinLinkDto);

    ParentJoinLinkDto findByJoinLinkAddr(String joinLinkAddr);

    List<ParentJoinLinkDto> findByStudentId(String studentId);

    int deleteParentJoinLink(Long linkId);

}
