package kr.or.ddit.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.log.AdminAuditLogDto;
import kr.or.ddit.finalProject.dto.log.LoginLogDto;
import kr.or.ddit.finalProject.dto.log.MemberActivityLogDto;
import kr.or.ddit.finalProject.dto.log.MemberLoginLogDto;
import kr.or.ddit.finalProject.dto.log.SystemErrorLogDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.mapper.PrincipalSystemMonitoringMapper;
import kr.or.ddit.service.PrincipalSystemMonitoringService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrincipalSystemMonitoringServiceImpl implements PrincipalSystemMonitoringService {

    private final PrincipalSystemMonitoringMapper mapper;

    @Override
    public PageResponse<LoginLogDto> searchAdminLoginLog(PaginationInfo<Map<String, Object>> paging) {
        List<LoginLogDto> items = mapper.searchAdminLoginLog(paging);
        int total = mapper.countAdminLoginLog(paging);
        return new PageResponse<>(items, total);
    }

    @Override
    public PageResponse<AdminAuditLogDto> searchAdminAuditLog(PaginationInfo<Map<String, Object>> paging) {
        List<AdminAuditLogDto> items = mapper.searchAdminAuditLog(paging);
        int total = mapper.countAdminAuditLog(paging);
        return new PageResponse<>(items, total);
    }

    @Override
    public PageResponse<MemberLoginLogDto> searchMemberLoginLog(PaginationInfo<Map<String, Object>> paging) {
        List<MemberLoginLogDto> items = mapper.searchMemberLoginLog(paging);
        int total = mapper.countMemberLoginLog(paging);
        return new PageResponse<>(items, total);
    }

    @Override
    public PageResponse<MemberActivityLogDto> searchMemberActivityLog(PaginationInfo<Map<String, Object>> paging) {
        List<MemberActivityLogDto> items = mapper.searchMemberActivityLog(paging);
        int total = mapper.countMemberActivityLog(paging);
        return new PageResponse<>(items, total);
    }

    @Override
    public PageResponse<SystemErrorLogDto> searchSystemErrorLog(PaginationInfo<Map<String, Object>> paging) {
        List<SystemErrorLogDto> items = mapper.searchSystemErrorLog(paging);
        int total = mapper.countSystemErrorLog(paging);
        return new PageResponse<>(items, total);
    }

    @Override
    public Map<String, Object> getSummaryStats() {
        return mapper.selectSummaryStats();
    }
}
