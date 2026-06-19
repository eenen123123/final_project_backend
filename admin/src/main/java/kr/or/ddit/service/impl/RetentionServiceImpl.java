package kr.or.ddit.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.retention.RetentionAttendanceDto;
import kr.or.ddit.finalProject.dto.retention.RetentionProcessDto;
import kr.or.ddit.finalProject.dto.retention.RetentionSummaryDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.mapper.RetentionMapper;
import kr.or.ddit.service.RetentionService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RetentionServiceImpl implements RetentionService {

    private final RetentionMapper mapper;

    @Override
    public RetentionSummaryDto getSummary() {
        return mapper.selectSummary();
    }

    @Override
    public PageResponse<RetentionAttendanceDto> searchAnomalies(PaginationInfo<Map<String, Object>> paging) {
        return new PageResponse<>(mapper.searchAnomalies(paging), mapper.countAnomalies(paging));
    }

    @Override
    public PageResponse<RetentionProcessDto> searchProcesses(PaginationInfo<Map<String, Object>> paging) {
        return new PageResponse<>(mapper.searchProcesses(paging), mapper.countProcesses(paging));
    }

    @Override
    public List<RetentionProcessDto> getProcessCalendar(String start, String end) {
        return mapper.selectProcessCalendar(start, end);
    }

    @Override
    @Transactional
    public void saveProcess(RetentionProcessDto dto) {
        mapper.insertProcess(dto);
    }
}
