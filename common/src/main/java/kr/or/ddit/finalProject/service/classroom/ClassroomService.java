package kr.or.ddit.finalProject.service.classroom;

import java.util.List;
import java.util.Map;

import kr.or.ddit.finalProject.dto.classroom.AchievementDto;
import kr.or.ddit.finalProject.dto.classroom.CalendarDayDto;
import kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse;
import kr.or.ddit.finalProject.dto.classroom.ClassroomGradeDto;
import kr.or.ddit.finalProject.dto.classroom.ClassroomListResponse;
import kr.or.ddit.finalProject.dto.classroom.TodayQuestionDto;
import kr.or.ddit.finalProject.dto.classroom.WeeklyDayDto;
import kr.or.ddit.finalProject.dto.lecture.ClassroomLectureResponse;

public interface ClassroomService {

    // 강사 본인이 담당하는 클래스룸 목록
    List<ClassroomListResponse> retrieveClassroomList(String instrUserId);

    // 학생 본인이 수강 중인 클래스룸 목록
    List<ClassroomListResponse> retrieveMyClassrooms(String userId);

    // 클래스룸 단건 상세 (강좌명·강사명·수강생수·운영기간 등 포함)
    ClassroomDetailResponse retrieveClassroomDetail(Long classSn);

    // 수강생별 과제 평균 점수 기반 성적 목록
    List<ClassroomGradeDto> retrieveGradeList(Long classSn);

    // 강의 목록 + 각 강의별 수강생 완료 인원수
    List<ClassroomLectureResponse> retrieveLecturesWithProgress(Long classSn);

    // 최근 7일간 일별 학습 접속 데이터 (홈 차트용)
    List<WeeklyDayDto> retrieveWeeklyData(Long classSn);

    // 전주 대비 이번 주 학습량 변화 텍스트 (예: "지난주보다 12% 증가")
    String retrieveWeeklyCompareText(Long classSn);

    // 클래스룸 달성 현황 (과제 제출률·강의 완료율 등 뱃지 목록)
    List<AchievementDto> retrieveAchievements(Long classSn);

    // 해당 월의 일별 학습 이벤트 (캘린더 렌더링용)
    List<CalendarDayDto> retrieveCalendarDays(Long classSn, int year, int month);

    // 캘린더 1일 이전 빈 칸 수 (요일 맞춤용 패딩)
    List<String> retrieveCalendarPadding(int year, int month);

    // 마감이 임박한 과제 수 (탭 뱃지 표시용)
    int retrieveUpcomingAssignmentCount(Long classSn);

    // 오늘의 학습 질문 (홈 위젯용, 없으면 null)
    TodayQuestionDto retrieveTodayQuestion(Long classSn);

    // 최근 7일간 학습 기록이 없는 비활성 수강생 수
    int retrieveInactiveStudentCount(Long classSn);

    // 클래스 내 전체 수강생의 개인별 강의 진도율 반환 (userId → progressRate, 탈퇴/취소 수강생 포함)
    // retrieveClassroomDetail과 분리: 진도율이 필요한 홈·수강생 탭에서만 호출
    Map<String, Double> retrieveProgressRates(Long classSn);
}
