package kr.or.ddit.finalProject.service.enrollment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.enrollment.CourseEnrollmentDto;
import kr.or.ddit.finalProject.mapper.enrollment.CourseEnrollmentMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseEnrollmentServiceImpl implements CourseEnrollmentService {

    private final CourseEnrollmentMapper enrollmentMapper;

    @Override
    @Transactional
    public void grantOrExtend(String userId, Long courseSn, Long ordSn) {
        CourseEnrollmentDto existing = enrollmentMapper.selectByUserAndCourse(userId, courseSn);
        if (existing == null) {
            enrollmentMapper.insertEnrollment(userId, courseSn, ordSn);
        } else {
            enrollmentMapper.extendEnrollment(userId, courseSn, ordSn);
        }
    }

    @Override
    public boolean hasActiveAccess(String userId, Long courseSn) {
        return enrollmentMapper.countActiveAccess(userId, courseSn) > 0;
    }
}
