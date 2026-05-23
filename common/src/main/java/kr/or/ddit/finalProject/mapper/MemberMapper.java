package kr.or.ddit.finalProject.mapper;

import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import kr.or.ddit.finalProject.dto.member.MemberDto;

@Mapper
public interface MemberMapper {

    Optional<MemberDto> findByUserId(String userId);

    int insertMember(MemberDto member);

    List<MemberDto> findAllMembers();
}
