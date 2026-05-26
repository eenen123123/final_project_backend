package kr.or.ddit.instructor;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import kr.or.ddit.dto.instructor.CourseDto;
import kr.or.ddit.instructor.mapper.InstructorCourseMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class InstructorCourseMapperTest {

    @Autowired
    private InstructorCourseMapper courseMapper;

    /**
     * Step 1: 실제 DB에 존재하는 강사 ID와 커리큘럼 ID를 확인하기 위해 먼저 이 테스트를 실행하세요. 로그에서
     * instrUserId와 curriculumId 값을 확인한 뒤 Step 2를 실행하세요.
     */
    @Test
    void step1_조회로_사용할_ID_확인() {
        // TODO: 아래 ID를 실제 존재하는 강사 ID로 교체하세요.
        String instrUserId = "testinstructor01";

        List<CourseDto> courses = courseMapper.selectCourseListByInstructor(instrUserId);
        log.info("현재 강좌 수: {}", courses.size());
        courses.forEach(c -> log.info("강좌: {}", c));
    }

    /**
     * Step 2: step1 확인 후 아래 instrUserId와 curriculumId를 실제 값으로 교체하고 실행하세요. 테스트
     * 데이터는 DB에 영구적으로 저장됩니다.
     */
    @Test
    void step2_강좌_테스트_데이터_삽입() {
        // ↓↓↓ 실제 값으로 교체 필요 ↓↓↓
        String instrUserId = "testinstructor01";    // 실제 강사 USER_ID
        Long curriculumId = 3L;         // 실제 CURRICULUM_MASTER.CURRICULUM_ID

        // 과목 데이터 미구축 상태이므로 임의값 사용 (FK 제약 없을 경우 통과)
        Long dummySubjClId = 1L;
        Long dummySubjId   = 1L;

        CourseDto course1 = new CourseDto();
        course1.setCurriculumId(curriculumId);
        course1.setSubjClId(dummySubjClId);
        course1.setSubjId(dummySubjId);
        course1.setInstrUserId(instrUserId);
        course1.setCourseNm("Java 백엔드 개발 기초");
        course1.setCourseExplnCn("Java 언어를 기반으로 백엔드 개발의 기초를 학습하는 강좌입니다.");
        course1.setTotLrnTimeCnt("12:30:00");
        course1.setOpnnYn("Y");
        course1.setProdMthdCd("01");
        course1.setCoursePrice(150_000L);
        course1.setRgtrId(instrUserId);
        course1.setLastMdfrId(instrUserId);

        CourseDto course2 = new CourseDto();
        course2.setCurriculumId(curriculumId);
        course2.setSubjClId(dummySubjClId);
        course2.setSubjId(dummySubjId);
        course2.setInstrUserId(instrUserId);
        course2.setCourseNm("Spring Boot 실전 프로젝트");
        course2.setCourseExplnCn("Spring Boot로 실제 서비스 수준의 웹 애플리케이션을 구축하는 심화 강좌입니다.");
        course2.setTotLrnTimeCnt("20:00:00");
        course2.setOpnnYn("Y");
        course2.setProdMthdCd("01");
        course2.setCoursePrice(200_000L);
        course2.setRgtrId(instrUserId);
        course2.setLastMdfrId(instrUserId);

        int result1 = courseMapper.insertCourse(course1);
        int result2 = courseMapper.insertCourse(course2);

        log.info("삽입 결과: course1={}, course2={}", result1, result2);

        // 삽입 후 목록 재조회
        List<CourseDto> inserted = courseMapper.selectCourseListByInstructor(instrUserId);
        log.info("삽입 후 강좌 수: {}", inserted.size());
        inserted.forEach(c -> log.info("  → {}", c.getCourseNm()));
    }
}
