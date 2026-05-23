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

    /**
     * 주어진 사용자ID 목록이 모두 존재하는지 확인하는 메서드
     * 
     * @param userIds 확인할 사용자ID 목록
     * @return 존재하는 사용자ID의 수 (userIds.size()와 같으면 모두 존재, 작으면 일부 또는 모두 존재하지 않음)
     */
    int isAllExistUsers(List<String> userIds);

}
