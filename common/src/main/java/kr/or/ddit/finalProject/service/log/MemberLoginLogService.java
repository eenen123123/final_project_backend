package kr.or.ddit.finalProject.service.log;

public interface MemberLoginLogService {
    void recordLoginSuccess(String inputUserId, String userId, String loginIp, String userAgent);
    void recordLoginFailure(String inputUserId, String userId, String failRsn, String loginIp, String userAgent);
    void recordLogout(String userId);
    void closeAllOpenSessions();
}
