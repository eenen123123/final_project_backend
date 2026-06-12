package kr.or.ddit.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.log.AdminAuditLogDto;
import kr.or.ddit.finalProject.dto.log.LoginLogDto;
import kr.or.ddit.finalProject.dto.log.MemberActivityLogDto;
import kr.or.ddit.finalProject.dto.log.MemberLoginLogDto;
import kr.or.ddit.finalProject.dto.log.SystemErrorLogDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

@Mapper
public interface PrincipalSystemMonitoringMapper {

    // ── 관리자 접속 이력 (ADMINLOGIN_LOG) ──────────────────────────────
    List<LoginLogDto> searchAdminLoginLog(PaginationInfo<Map<String, Object>> paging);
    int countAdminLoginLog(PaginationInfo<Map<String, Object>> paging);

    // ── 관리자 활동 이력 (HERMES_ADMIN_AUDIT_LOG) ─────────────────
    List<AdminAuditLogDto> searchAdminAuditLog(PaginationInfo<Map<String, Object>> paging);
    int countAdminAuditLog(PaginationInfo<Map<String, Object>> paging);

    // ── 사용자 접속 이력 (MEMBER_LOGIN_LOG) ───────────────────────────
    List<MemberLoginLogDto> searchMemberLoginLog(PaginationInfo<Map<String, Object>> paging);
    int countMemberLoginLog(PaginationInfo<Map<String, Object>> paging);

    // ── 사용자 활동 이력 (MEMBER_ACTIVITY_LOG) ────────────────────────
    List<MemberActivityLogDto> searchMemberActivityLog(PaginationInfo<Map<String, Object>> paging);
    int countMemberActivityLog(PaginationInfo<Map<String, Object>> paging);

    // ── 시스템 에러 로그 (SYSTEM_ERROR_LOG) ───────────────────────────
    List<SystemErrorLogDto> searchSystemErrorLog(PaginationInfo<Map<String, Object>> paging);
    int countSystemErrorLog(PaginationInfo<Map<String, Object>> paging);

    // ── 상단 요약 카드 통계 ────────────────────────────────────────────
    Map<String, Object> selectSummaryStats();

    // ── 에러 추적 (traceId → 회원/관리자/익명 판별 및 활동 이력) ──────────
    String findMemberIdByTraceId(@Param("traceId") String traceId);
    String findAdminIdByTraceId(@Param("traceId") String traceId);
    List<MemberActivityLogDto> findRecentMemberActivities(@Param("userId") String userId);
    List<AdminAuditLogDto> findRecentAdminAudits(@Param("adminId") String adminId);
    String findRequestIpByTraceId(@Param("traceId") String traceId);
    List<SystemErrorLogDto> findErrorsByIp(@Param("ip") String ip);
}
