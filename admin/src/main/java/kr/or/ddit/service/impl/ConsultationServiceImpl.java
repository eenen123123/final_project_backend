package kr.or.ddit.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.consultation.ConsultationDto;
import kr.or.ddit.finalProject.dto.consultation.ConsultationStudentDto;
import kr.or.ddit.finalProject.dto.consultation.ConsultationSummaryDto;
import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.mapper.ConsultationMapper;
import kr.or.ddit.service.ConsultationService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultationServiceImpl implements ConsultationService {

    private final ConsultationMapper mapper;

    @Override
    public ConsultationSummaryDto getSummary() {
        return mapper.selectSummary();
    }

    @Override
    public PageResponse<ConsultationDto> searchConsultations(PaginationInfo<Map<String, Object>> paging) {
        return new PageResponse<>(mapper.searchConsultations(paging), mapper.countConsultations(paging));
    }

    @Override
    public PageResponse<ConsultationStudentDto> searchStudents(PaginationInfo<Map<String, Object>> paging) {
        return new PageResponse<>(mapper.searchStudents(paging), mapper.countStudents(paging));
    }

    @Override
    public List<ConsultationDto> getByStudent(String stdUserId) {
        return mapper.selectByStudent(stdUserId);
    }

    @Override
    public List<ConsultationDto> getForCalendar(String start, String end) {
        return mapper.selectForCalendar(start, end);
    }

    @Override
    public ConsultationStudentDto getStudentInfo(String stdUserId) {
        return mapper.selectStudentInfo(stdUserId);
    }

    @Override
    public List<CourseDto> getStudentCourses(String stdUserId) {
        return mapper.selectStudentCourses(stdUserId);
    }

    @Override
    public ConsultationDto getDetail(Long cnslSn) {
        return mapper.selectById(cnslSn);
    }

    @Override
    @Transactional
    public void saveConsultation(ConsultationDto dto) {
        mapper.insertConsultation(dto);
    }
}
