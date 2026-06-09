package kr.or.ddit.service;

import kr.or.ddit.dto.AuditLogDto;
import kr.or.ddit.mapper.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogMapper auditLogMapper;

    @Async
    public void save(AuditLogDto dto) {
        try {
            auditLogMapper.insertAuditLog(dto);
        } catch (Exception e) {
            log.error("[AUDIT] DB 저장 실패: uri={}, reason={}", dto.getRequestUri(), e.getMessage());
        }
    }
}
