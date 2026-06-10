package kr.or.ddit.service.staff;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.course.CourseSearchCondition;
import kr.or.ddit.finalProject.mapper.course.CourseMapper;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class PdTest {

    @Autowired
    private CourseMapper courseMapper;

    @Test
    public void testGetCourseList() {
        PaginationInfo<CourseSearchCondition> paginationInfo = new PaginationInfo<>(100, 1);
        var courseList = courseMapper.selectCourseList(paginationInfo);
        for (CourseDto course : courseList) {
            log.info("Course: {}", course);
        }

    }
}
