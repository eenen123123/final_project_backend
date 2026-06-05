package kr.or.ddit.finalProject.service.classroom;

import java.util.List;

import kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse;
import kr.or.ddit.finalProject.dto.classroom.ClassroomGradeDto;
import kr.or.ddit.finalProject.dto.classroom.ClassroomListResponse;
import kr.or.ddit.finalProject.dto.lecture.LectureProgressDto;

public interface ClassroomService {

    List<ClassroomListResponse> retrieveClassroomList(String instrUserId);

    ClassroomDetailResponse retrieveClassroomDetail(Long classSn);

    List<ClassroomGradeDto> retrieveGradeList(Long classSn);

    List<LectureProgressDto> retrieveLecturesWithProgress(Long classSn);

}
