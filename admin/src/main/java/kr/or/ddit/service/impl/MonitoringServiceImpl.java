package kr.or.ddit.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.monitoring.ClassroomOverviewDto;
import kr.or.ddit.mapper.MonitoringMapper;
import kr.or.ddit.service.MonitoringService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonitoringServiceImpl implements MonitoringService {

    private final MonitoringMapper monitoringMapper;

    @Override
    public List<ClassroomOverviewDto> getClassroomOverview() {
        List<ClassroomOverviewDto> list = monitoringMapper.selectClassroomOverview();
        for (ClassroomOverviewDto dto : list) {
            if (dto.getLectureCnt() > 0) {
                dto.setCompletedLectureAvg(
                    (int) Math.round(dto.getAvgProgressRate() * dto.getLectureCnt() / 100.0)
                );
            }
        }
        return list;
    }
}
