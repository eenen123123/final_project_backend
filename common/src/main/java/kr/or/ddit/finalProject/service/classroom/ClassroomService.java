package kr.or.ddit.finalProject.service.classroom;

import java.util.List;

import kr.or.ddit.finalProject.dto.classroom.AchievementDto;
import kr.or.ddit.finalProject.dto.classroom.CalendarDayDto;
import kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse;
import kr.or.ddit.finalProject.dto.classroom.ClassroomGradeDto;
import kr.or.ddit.finalProject.dto.classroom.ClassroomListResponse;
import kr.or.ddit.finalProject.dto.classroom.TodayQuestionDto;
import kr.or.ddit.finalProject.dto.classroom.WeeklyDayDto;
import kr.or.ddit.finalProject.dto.lecture.ClassroomLectureResponse;

public interface ClassroomService {

    List<ClassroomListResponse> retrieveClassroomList(String instrUserId);

    List<ClassroomListResponse> retrieveMyClassrooms(String userId);

    ClassroomDetailResponse retrieveClassroomDetail(Long classSn);

    List<ClassroomGradeDto> retrieveGradeList(Long classSn);

    List<ClassroomLectureResponse> retrieveLecturesWithProgress(Long classSn);

    List<WeeklyDayDto> retrieveWeeklyData(Long classSn);

    String retrieveWeeklyCompareText(Long classSn);

    List<AchievementDto> retrieveAchievements(Long classSn);

    List<CalendarDayDto> retrieveCalendarDays(Long classSn, int year, int month);

    List<String> retrieveCalendarPadding(int year, int month);

    int retrieveUpcomingAssignmentCount(Long classSn);

    TodayQuestionDto retrieveTodayQuestion(Long classSn);

    int retrieveInactiveStudentCount(Long classSn);
}
