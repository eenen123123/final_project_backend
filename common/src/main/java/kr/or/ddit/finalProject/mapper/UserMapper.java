package kr.or.ddit.finalProject.mapper;

import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import kr.or.ddit.finalProject.dto.user.MemberDto;

@Mapper
public interface UserMapper {

    /**
     * LoginId를 받아서 이미 존재하는 회원인지 조회
     * 
     * @param userId 조회할 Id
     * @return 존재 여부
     */
    boolean existsByUserId(String userId);

    /**
     * Nickname을 입력받아서 이미 존재하는 닉네임인지 조회
     * 
     * @param nickname 조회할 Nickname
     * @return 존재 여부
     */

    Optional<MemberDto> findByUserId(String userId);

    int insertUser(MemberDto userDto);

}
