package kr.or.ddit.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.retention.RetentionAttendanceDto;
import kr.or.ddit.finalProject.dto.retention.RetentionProcessDto;
import kr.or.ddit.finalProject.dto.retention.RetentionSummaryDto;
import kr.or.ddit.finalProject.dto.student.StudentAttendanceDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

/**
 * 퇴원 방어 및 유지 관리 매퍼
 * - 근태 특이사항 집계 (STUDENT_ATTENDANCE · 최근 30일 · 오프라인 학생)
 * - 퇴원 방어 상담 프로세스 이력 (RETENTION_PROCESS)
 * - 근태 Excel 업로드 적재
 */
@Mapper
public interface RetentionMapper {

    // ── 상단 요약 카드 ────────────────────────────────────────
    RetentionSummaryDto selectSummary();

    // ── 근태 특이사항 집계 (검색·페이징) ─────────────────────
    List<RetentionAttendanceDto> searchAnomalies(PaginationInfo<Map<String, Object>> paging);
    int countAnomalies(PaginationInfo<Map<String, Object>> paging);

    // ── 상담 프로세스 이력 (학생별 집계 · 검색·페이징) ───────
    List<RetentionProcessDto> searchProcesses(PaginationInfo<Map<String, Object>> paging);
    int countProcesses(PaginationInfo<Map<String, Object>> paging);

    // ── 상담 프로세스 캘린더 (기간 조회) ─────────────────────
    List<RetentionProcessDto> selectProcessCalendar(@Param("start") String start, @Param("end") String end);

    // ── 상담 프로세스 저장 ───────────────────────────────────
    int insertProcess(RetentionProcessDto dto);

    // ── 근태 Excel 업로드 적재 ───────────────────────────────
    int existsStudent(@Param("stdUserId") String stdUserId);
    void insertAttendance(StudentAttendanceDto dto);
}
