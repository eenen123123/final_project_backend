package kr.or.ddit.service;

import java.util.Map;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.log.AdminAuditLogDto;
import kr.or.ddit.finalProject.dto.log.LoginLogDto;
import kr.or.ddit.finalProject.dto.log.MemberActivityLogDto;
import kr.or.ddit.finalProject.dto.log.MemberLoginLogDto;
import kr.or.ddit.finalProject.dto.log.SystemErrorLogDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

public interface PrincipalSystemMonitoringService {

    PageResponse<LoginLogDto> searchAdminLoginLog(PaginationInfo<Map<String, Object>> paging);

    PageResponse<AdminAuditLogDto> searchAdminAuditLog(PaginationInfo<Map<String, Object>> paging);

    PageResponse<MemberLoginLogDto> searchMemberLoginLog(PaginationInfo<Map<String, Object>> paging);

    PageResponse<MemberActivityLogDto> searchMemberActivityLog(PaginationInfo<Map<String, Object>> paging);

    PageResponse<SystemErrorLogDto> searchSystemErrorLog(PaginationInfo<Map<String, Object>> paging);

    Map<String, Object> getSummaryStats();
}
