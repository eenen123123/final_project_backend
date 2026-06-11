package kr.or.ddit.finalProject.service.lecture;

import java.util.List;

import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.lecture.LectureDto;
import kr.or.ddit.finalProject.dto.lecture.LectureResponseDto;

public interface LectureService {

    List<LectureDto> retrieveLectureByCourseSn(Long courseSn);

    LectureDto retrieveLectureBySn(Long lectureSn);

    boolean createLecture(LectureDto lectureDto);

    void modifyLecture(LectureDto lectureDto, String currentUserId);

    void removeLecture(Long lectureSn, String currentUserId);

    List<LectureResponseDto> retrieveLectureListByCourseSn(Long courseSn);
}
