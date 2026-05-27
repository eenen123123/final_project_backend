package kr.or.ddit.finalProject.service.course;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.mapper.course.CourseMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;

    @Override
    public List<CourseDto> retrieveCourseList(String instrUserId) {
        return courseMapper.selectCourseListByInstructor(instrUserId);
    }
}
