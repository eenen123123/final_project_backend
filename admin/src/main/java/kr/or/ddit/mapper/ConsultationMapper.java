package kr.or.ddit.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.consultation.ConsultationDto;
import kr.or.ddit.finalProject.dto.consultation.ConsultationStudentDto;
import kr.or.ddit.finalProject.dto.consultation.ConsultationSummaryDto;
import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

/**
 * 학부모/학생 상담 관리 매퍼
 * - 전체 상담 이력 (검색·페이징)
 * - 학생 모달 검색 (검색·페이징)
 * - 학생별 상담 이력 / 상담 상세
 * - 학부모 자동 매칭
 * - 상담 기록 저장
 */
@Mapper
public interface ConsultationMapper {

    // ── 상단 요약 카드 ────────────────────────────────────────
    ConsultationSummaryDto selectSummary();

    // ── 전체 상담 이력 (검색·페이징) ─────────────────────────
    List<ConsultationDto> searchConsultations(PaginationInfo<Map<String, Object>> paging);
    int countConsultations(PaginationInfo<Map<String, Object>> paging);

    // ── 학생 모달 검색 (검색·페이징) ─────────────────────────
    List<ConsultationStudentDto> searchStudents(PaginationInfo<Map<String, Object>> paging);
    int countStudents(PaginationInfo<Map<String, Object>> paging);

    // ── 학생별 상담 이력 (비페이징 · 타임라인) ───────────────
    List<ConsultationDto> selectByStudent(@Param("stdUserId") String stdUserId);

    // ── 캘린더 표시용 (기간 조회) ────────────────────────────
    List<ConsultationDto> selectForCalendar(@Param("start") String start, @Param("end") String end);

    // ── 학생 1인 정보 + 학부모 자동 매칭 ─────────────────────
    ConsultationStudentDto selectStudentInfo(@Param("stdUserId") String stdUserId);

    // ── 학생이 수강 중인 강좌 (상담 상세 모달) ───────────────
    List<CourseDto> selectStudentCourses(@Param("stdUserId") String stdUserId);

    // ── 상담 상세 (보기 모달) ────────────────────────────────
    ConsultationDto selectById(@Param("cnslSn") Long cnslSn);

    // ── 상담 기록 저장 ───────────────────────────────────────
    int insertConsultation(ConsultationDto dto);
}
