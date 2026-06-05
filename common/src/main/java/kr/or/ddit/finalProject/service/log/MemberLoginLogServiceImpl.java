package kr.or.ddit.finalProject.service.log;

import kr.or.ddit.finalProject.dto.log.MemberLoginLogDto;
import kr.or.ddit.finalProject.mapper.log.MemberLoginLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberLoginLogServiceImpl implements MemberLoginLogService {

    private final MemberLoginLogMapper memberLoginLogMapper;

    @Override
    public void recordLoginSuccess(String inputUserId, String userId, String loginIp, String userAgent) {
        memberLoginLogMapper.insertMemberLoginLog(
            MemberLoginLogDto.builder()
                .inputUserId(inputUserId)
                .userId(userId)
                .onlineYn("Y")
                .loginSuccessYn("Y")
                .loginIp(loginIp)
                .build()
        );
    }

    @Override
    public void recordLoginFailure(String inputUserId, String userId, String failRsn, String loginIp, String userAgent) {
        memberLoginLogMapper.insertMemberLoginLog(
            MemberLoginLogDto.builder()
                .inputUserId(inputUserId)
                .userId(userId)   // USER_NOT_FOUND면 null, 그 외엔 실제 userId
                .onlineYn("N")
                .loginSuccessYn("N")
                .loginIp(loginIp)
                .failRsn(failRsn)
                .build()
        );
    }

    @Override
    public void recordLogout(String userId) {
        memberLoginLogMapper.updateMemberLogout(userId);
    }

    @Override
    public void closeAllOpenSessions() {
        memberLoginLogMapper.closeAllOpenSessions();
    }
}
