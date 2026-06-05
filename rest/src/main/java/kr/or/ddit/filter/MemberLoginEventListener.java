package kr.or.ddit.filter;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import kr.or.ddit.finalProject.service.log.MemberLoginLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberLoginEventListener {

    private final MemberLoginLogService memberLoginLogService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        memberLoginLogService.closeAllOpenSessions();
        log.info("[MemberLoginLog] 서버 재시작 - 미처리 세션 일괄 정리 완료");
    }
}
