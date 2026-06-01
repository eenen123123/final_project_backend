package kr.or.ddit.finalProject.service.lecture;

import java.util.List;

import kr.or.ddit.finalProject.dto.lecture.LectureDto;

public interface LectureService {

    List<LectureDto> retrieveLectureByCourseSn(Long courseSn);

    boolean createLecture(LectureDto lectureDto);

    void modifyLecture(LectureDto lectureDto, String currentUserId);

    void removeLecture(Long lectureSn, String currentUserId);

}
