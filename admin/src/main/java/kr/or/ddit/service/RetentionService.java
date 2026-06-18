package kr.or.ddit.service;

import java.util.List;
import java.util.Map;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.retention.RetentionAttendanceDto;
import kr.or.ddit.finalProject.dto.retention.RetentionProcessDto;
import kr.or.ddit.finalProject.dto.retention.RetentionSummaryDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

/**
 * 퇴원 방어 및 유지 관리 서비스
 */
public interface RetentionService {

    /** 상단 요약 카드 */
    RetentionSummaryDto getSummary();

    /** 근태 특이사항 집계 (검색·페이징) */
    PageResponse<RetentionAttendanceDto> searchAnomalies(PaginationInfo<Map<String, Object>> paging);

    /** 상담 프로세스 이력 (검색·페이징) */
    PageResponse<RetentionProcessDto> searchProcesses(PaginationInfo<Map<String, Object>> paging);

    /** 상담 프로세스 캘린더 (기간 조회) */
    List<RetentionProcessDto> getProcessCalendar(String start, String end);

    /** 상담 프로세스 저장 */
    void saveProcess(RetentionProcessDto dto);
}
