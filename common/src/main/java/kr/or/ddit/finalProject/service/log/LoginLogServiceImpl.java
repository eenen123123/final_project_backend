package kr.or.ddit.finalProject.service.log;

import java.util.List;
import org.springframework.stereotype.Service;
import kr.or.ddit.finalProject.dto.log.LoginLogDto;
import kr.or.ddit.finalProject.mapper.log.LoginLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 로그인 로그 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl implements LoginLogService {

    private final LoginLogMapper loginLogMapper;

    /**
     * 로그인 로그 기록
     * @param userId // 사용자 아이디
     * @param ip // 로그인 IP 주소
     * @param sessionId // 세션 ID
     */
    @Override
    public void saveLoginLog(String userId, String ip, String sessionId) {
        LoginLogDto dto = new LoginLogDto();
        dto.setUserId(userId);
        dto.setLoginIp(ip);
        dto.setSessionId(sessionId);
        loginLogMapper.insertLoginLog(dto);
        log.info("[LoginLog] 로그인 - userId={}, ip={}", userId, ip);
    }

    /**
     * 로그아웃 일시 업데이트
     * @param sessionId // 세션 ID
     */
    @Override
    public void saveLogoutLog(String sessionId) {
        loginLogMapper.updateLogoutDt(sessionId);
        log.info("[LoginLog] 로그아웃 - sessionId={}", sessionId);
    }

    /**
     * 사용자별 로그인 로그 조회
     * @param userId // 사용자 아이디
     * @return
     */
    @Override
    public List<LoginLogDto> getLoginLogsByUserId(String userId) {
        return loginLogMapper.selectLoginLogsByUserId(userId);
    }

    /**
     * 사용자별 마지막 로그인 로그 조회
     * @return
     */
    @Override
    public List<LoginLogDto> getLastLoginPerUser() {
        return loginLogMapper.selectLastLoginPerUser();
    }
}
