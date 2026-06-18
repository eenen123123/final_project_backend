package kr.or.ddit.service;

import java.util.List;
import java.util.Map;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.consultation.ConsultationDto;
import kr.or.ddit.finalProject.dto.consultation.ConsultationStudentDto;
import kr.or.ddit.finalProject.dto.consultation.ConsultationSummaryDto;
import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

/**
 * 학부모/학생 상담 관리 서비스
 */
public interface ConsultationService {

    /** 상단 요약 카드 */
    ConsultationSummaryDto getSummary();

    /** 전체 상담 이력 (검색·페이징) */
    PageResponse<ConsultationDto> searchConsultations(PaginationInfo<Map<String, Object>> paging);

    /** 학생 모달 검색 (검색·페이징) */
    PageResponse<ConsultationStudentDto> searchStudents(PaginationInfo<Map<String, Object>> paging);

    /** 학생별 상담 이력 (비페이징 타임라인) */
    List<ConsultationDto> getByStudent(String stdUserId);

    /** 캘린더 표시용 (기간 조회) */
    List<ConsultationDto> getForCalendar(String start, String end);

    /** 학생 1인 정보 + 학부모 자동 매칭 */
    ConsultationStudentDto getStudentInfo(String stdUserId);

    /** 학생이 수강 중인 강좌 (상담 상세 모달) */
    List<CourseDto> getStudentCourses(String stdUserId);

    /** 상담 상세 (보기 모달) */
    ConsultationDto getDetail(Long cnslSn);

    /** 상담 기록 저장 */
    void saveConsultation(ConsultationDto dto);
}
