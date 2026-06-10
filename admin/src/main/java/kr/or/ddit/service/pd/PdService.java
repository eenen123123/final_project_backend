package kr.or.ddit.service.pd;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import kr.or.ddit.finalProject.dto.course.AdminCourseSearchCondition;
import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.mapper.course.CourseMapper;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PdService {

    private final CourseMapper courseMapper;

    public List<CourseDto> getCourseList() {
        PaginationInfo<AdminCourseSearchCondition> paginationInfo = new PaginationInfo<>(100, 1);
        return courseMapper.selectCourseList(paginationInfo);
    }

    public List<Map<String, Object>> getCourseOptions() {
        return getCourseList().stream().map(c -> {
            Map<String, Object> opt = new java.util.LinkedHashMap<>();
            opt.put("sn", c.getCourseSn());
            opt.put("nm", c.getCourseNm());
            return opt;
        }).collect(Collectors.toList());
    }
}
