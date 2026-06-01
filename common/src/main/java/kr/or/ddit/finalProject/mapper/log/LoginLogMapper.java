package kr.or.ddit.finalProject.mapper.log;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import kr.or.ddit.finalProject.dto.log.LoginLogDto;

/**
 * 로그인 로그 Mapper
 */
@Mapper
public interface LoginLogMapper {

    /**
     * 로그인 로그 기록
     * @param loginLogDto
     */
    void insertLoginLog(LoginLogDto loginLogDto);

    /**
     * 로그아웃 일시 업데이트
     * @param sessionId
     */
    void updateLogoutDt(@Param("sessionId") String sessionId);

    /**
     * 사용자별 로그인 로그 조회
     * @param userId
     * @return
     */
    List<LoginLogDto> selectLoginLogsByUserId(@Param("userId") String userId);
    
    /**
     * 사용자별 마지막 로그인 로그 조회
     * @return
     */
    List<LoginLogDto> selectLastLoginPerUser();
}
