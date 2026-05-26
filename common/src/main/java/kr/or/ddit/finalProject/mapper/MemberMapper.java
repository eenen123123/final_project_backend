package kr.or.ddit.finalProject.mapper;

import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.dto.member.MemberRoleDto;

@Mapper
public interface MemberMapper {

    Optional<MemberDto> findByUserId(String userId);

    int insertMember(MemberDto member);

    // 더미 데이터 생성
    int fakerMember(MemberDto member);

    // 더미 권한 생성
    int fakerRole(MemberRoleDto role);

    // 더미 권한 매핑 생성
    int fakerRoleMapping(MemberRoleDto roleMapping);
    
}
