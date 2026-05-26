package kr.or.ddit.instructor.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.dto.instructor.CourseDto;
import kr.or.ddit.instructor.mapper.InstructorCourseMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstructorCourseServiceImpl implements InstructorCourseService {

    private final InstructorCourseMapper courseMapper;

    @Override
    public List<CourseDto> retrieveCourseList(String instrUserId) {
        return courseMapper.selectCourseListByInstructor(instrUserId);
    }
}
