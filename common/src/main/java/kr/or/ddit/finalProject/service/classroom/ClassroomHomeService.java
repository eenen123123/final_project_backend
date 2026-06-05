package kr.or.ddit.finalProject.service.classroom;

import java.util.List;

import kr.or.ddit.finalProject.dto.classroom.AchievementDto;
import kr.or.ddit.finalProject.dto.classroom.CalendarDayDto;
import kr.or.ddit.finalProject.dto.classroom.TodayQuestionDto;
import kr.or.ddit.finalProject.dto.classroom.WeeklyDayDto;

public interface ClassroomHomeService {

    List<WeeklyDayDto> retrieveWeeklyData(Long classSn);

    String retrieveWeeklyCompareText(Long classSn);

    List<AchievementDto> retrieveAchievements(Long classSn);

    /** 이번 달 캘린더 날짜 목록 (이벤트 있는 날 표시 포함) */
    List<CalendarDayDto> retrieveCalendarDays(Long classSn, int year, int month);

    /** 캘린더 앞 패딩 (빈 문자열 목록) */
    List<String> retrieveCalendarPadding(int year, int month);

    int retrieveUpcomingAssignmentCount(Long classSn);

    TodayQuestionDto retrieveTodayQuestion(Long classSn);
}
