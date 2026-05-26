package kr.or.ddit.finalProject.mapper;

import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.member.MemberDto;

@Mapper
public interface MemberMapper {

    Optional<MemberDto> findByUserId(String userId);

    int insertMember(MemberDto member);

    // 더미 데이터 생성
    int fakerMember(MemberDto member);

    // 더미 권한 부여
    int fakerRoleMapping(@Param("userId") String userId, @Param("role") String role);
    
}
