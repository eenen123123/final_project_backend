package kr.or.ddit.finalProject.service.log;

import java.util.List;
import kr.or.ddit.finalProject.dto.log.LoginLogDto;

/**
 * 로그인 로그 서비스 인터페이스
 */
public interface LoginLogService {

    /**
     * 로그인 로그 기록
     * @param userId
     * @param ip
     * @param sessionId
     */
    void saveLoginLog(String userId, String ip, String sessionId);

    /**
     * 로그아웃 일시 업데이트
     * @param sessionId
     */
    void saveLogoutLog(String sessionId);

    /**
     * 사용자별 로그인 로그 조회
     * @param userId
     * @return
     */
    List<LoginLogDto> getLoginLogsByUserId(String userId);

    /**
     * 사용자별 마지막 로그인 로그 조회
     * @return
     */
    List<LoginLogDto> getLastLoginPerUser();

    /**
     * 서버 재시작 시 미처리 세션 일괄 정리
     */
    void closeAllOpenSessions();
}
