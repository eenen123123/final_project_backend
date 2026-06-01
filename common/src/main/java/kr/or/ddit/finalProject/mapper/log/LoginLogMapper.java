package kr.or.ddit.finalProject.mapper.log;

import java.util.List;
import java.util.Map;
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

    /**
     * 서버 재시작 시 미처리(LOGOUT_DT IS NULL) 세션 일괄 정리
     */
    void closeAllOpenSessions();

    // 현재 활성 세션이 있는 USER_ID 목록 조회 (중복 로그인 허용 환경용)
    List<Map<String, String>> selectOnlineStatusPerUser();
}
