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
    public List<CourseDto> retrieveCoursesByInstructor(String instrUserId) {
        return courseMapper.selectCoursesByInstructor(instrUserId);
    }

    @Override
    public CourseDto retrieveCourseBySn(Long courseSn) {
        return courseMapper.selectCourseBySn(courseSn);
    }

    @Override
    @Transactional
    public void updateCourseAtchFileId(Long courseSn, String atchFileId) {
        courseMapper.updateCourseAtchFileId(courseSn, atchFileId);
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
        if (!currentUserId.equals(original.getInstrUserId())) {
            throw new SecurityException("본인이 작성한 강좌만 수정할 수 있습니다.");
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
        if (!currentUserId.equals(original.getInstrUserId())) {
            throw new SecurityException("본인이 작성한 강좌만 삭제할 수 있습니다.");
        }
        int lectureCount = courseMapper.countLectureByCourse(courseSn);
        if (lectureCount > 0) {
            throw new IllegalArgumentException("강의가 존재하는 강좌는 삭제할 수 없습니다. 강의를 먼저 삭제해 주세요.");
        }
        courseMapper.deleteCourse(courseSn);
    }

}
