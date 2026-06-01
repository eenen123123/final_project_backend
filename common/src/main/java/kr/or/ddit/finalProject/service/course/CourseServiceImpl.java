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
    public List<CourseDto> retrieveCourseByCurriculumId(Long curriculumId) {
        return courseMapper.selectCourseByCurriculumId(curriculumId);
    }

    @Override
    @Transactional
    public boolean createCourse(CourseDto courseDto) {
        return courseMapper.insertCourse(courseDto) > 0;
    }

    @Override
    @Transactional
    public void modifyCourse(CourseDto courseDto, String currentUserId) {
        CourseDto original = courseMapper.selectCourseBySn(courseDto.getCourseSn());
        if (original == null) {
            throw new IllegalArgumentException("존재하지 않는 강좌입니다.");
        }
        courseDto.setLastMdfrId(currentUserId);
        courseMapper.updateCourse(courseDto);
    }

    @Override
    @Transactional
    public void removeCourse(Long courseSn, String currentUserId) {
        CourseDto original = courseMapper.selectCourseBySn(courseSn);
        if (original == null) {
            throw new IllegalArgumentException("존재하지 않는 강좌입니다.");
        }
        courseMapper.deleteCourse(courseSn);
    }

}
