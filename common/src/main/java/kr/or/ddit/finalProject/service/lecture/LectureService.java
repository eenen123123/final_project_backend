package kr.or.ddit.finalProject.service.lecture;

import java.util.List;

import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.lecture.LectureDto;
import kr.or.ddit.finalProject.dto.lecture.LectureResponseDto;
import kr.or.ddit.finalProject.dto.lecture.LectureProgressDetailResponse;
import kr.or.ddit.finalProject.dto.lecture.StudentLectureProgressResponse;

public interface LectureService {

    List<LectureDto> retrieveLectureByCourseSn(Long courseSn);

    LectureDto retrieveLectureBySn(Long lectureSn);

    boolean createLecture(LectureDto lectureDto);

    void modifyLecture(LectureDto lectureDto, String currentUserId);

    void removeLecture(Long lectureSn, String currentUserId);

    List<LectureResponseDto> retrieveLectureListByCourseSn(Long courseSn, String userId);

    void updateLectureProgress(Long lectureId, Long courseId, Integer progress, String userId);

    void toggleOpnnYn(Long lectureSn, String userId);

    void toggleLockYn(Long lectureSn, String userId);

    List<StudentLectureProgressResponse> retrieveStudentProgressByLecture(Long classSn, Long lectureSn);

    List<LectureProgressDetailResponse> retrieveLectureProgressByStudent(Long classSn, String userId);
}
