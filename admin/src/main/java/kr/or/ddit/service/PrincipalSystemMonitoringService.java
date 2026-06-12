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

    /**
     * traceId로 회원/관리자/익명을 판단하고 해당 사용자의 활동 이력을 조회한다.
     */
    Map<String, Object> resolveTrace(String traceId);
}
