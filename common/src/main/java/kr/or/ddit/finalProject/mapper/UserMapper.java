package kr.or.ddit.finalProject.mapper;

import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import kr.or.ddit.finalProject.dto.user.UserDto;

@Mapper
public interface UserMapper {

    /**
     * LoginId를 받아서 이미 존재하는 회원인지 조회
     * 
     * @param loginId 조회할 Id
     * @return 존재 여부
     */
    boolean existsByLoginId(String loginId);

    /**
     * Nickname을 입력받아서 이미 존재하는 닉네임인지 조회
     * 
     * @param nickname 조회할 Nickname  
     * @return 존재 여부
     */
    boolean existsByNickname(String nickname);

    /**
     * LoginId를 받아서 해당 회원의 정보를 가져옴 
     * 
     * @param loginId 조회할 LoginId
     * @return 
     */
    Optional<UserDto> findByLoginId(String loginId);


    int insertUser(UserDto userDto);

}
