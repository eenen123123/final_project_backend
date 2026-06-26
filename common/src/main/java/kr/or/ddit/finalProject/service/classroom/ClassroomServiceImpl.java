package kr.or.ddit.finalProject.service.classroom;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.classroom.AchievementDto;
import kr.or.ddit.finalProject.dto.classroom.ClassroomDto;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.classroom.CalendarDayDto;
import kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse;
import kr.or.ddit.finalProject.dto.classroom.ClassroomGradeDto;
import kr.or.ddit.finalProject.dto.classroom.ClassroomListResponse;
import kr.or.ddit.finalProject.dto.classroom.ClassroomMemberListResponse;
import kr.or.ddit.finalProject.dto.classroom.TodayQuestionDto;
import kr.or.ddit.finalProject.dto.classroom.WeeklyDayDto;
import kr.or.ddit.finalProject.dto.coursecohort.CourseCohortListResponse;
import kr.or.ddit.finalProject.dto.lecture.ClassroomLectureResponse;
import kr.or.ddit.finalProject.mapper.classroom.ClassroomMapper;
import kr.or.ddit.finalProject.mapper.classroom.ClassroomMemberMapper;
import kr.or.ddit.finalProject.mapper.coursecohort.CourseCohortMapper;
import kr.or.ddit.finalProject.mapper.lecture.LectureMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassroomServiceImpl implements ClassroomService {

    private static final DateTimeFormatter REG_DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final double CIRCUMFERENCE = 2 * Math.PI * 20;
    private static final String[] DAY_LABELS = { "월", "화", "수", "목", "금", "토", "일" };

    private final ClassroomMapper classroomMapper;
    private final ClassroomMemberMapper classroomMemberMapper;
    private final CourseCohortMapper courseCohortMapper;
    private final LectureMapper lectureMapper;

    @Override
    public List<ClassroomListResponse> retrieveClassroomList(String instrUserId) {
        List<ClassroomListResponse> list = classroomMapper.selectClassroomListByInstructor(instrUserId);
        list.forEach(item -> item.setFormattedRegDt(item.getRegDt().format(REG_DT_FORMAT)));
        return list;
    }

    @Override
    public ClassroomDetailResponse retrieveClassroomDetail(Long classSn) {
        ClassroomDetailResponse detail = classroomMapper.selectClassroomBySn(classSn);
        if (detail == null) {
            throw new IllegalArgumentException("존재하지 않는 클래스룸입니다: " + classSn);
        }

        detail.setEnrlStrtYmd(formatYmd(detail.getEnrlStrtYmd()));
        if (detail.getEnrlEndYmd() != null) {
            detail.setEnrlEndYmd(formatYmd(detail.getEnrlEndYmd()));
        }

        List<ClassroomMemberListResponse> members = classroomMemberMapper.selectMembersByClassSn(classSn, 0, 9999);
        members.forEach(m -> m.setFormattedRegDt(m.getRegDt().format(REG_DT_FORMAT)));
        detail.setMembers(members);

        List<CourseCohortListResponse> cohorts = courseCohortMapper.selectCohortsByClassSn(classSn);
        cohorts.forEach(c -> {
            c.setCohortStrtYmd(formatYmd(c.getCohortStrtYmd()));
            if (c.getCohortEndYmd() != null) c.setCohortEndYmd(formatYmd(c.getCohortEndYmd()));
        });
        detail.setCohorts(cohorts);

        return detail;
    }

    @Override
    public List<ClassroomListResponse> retrieveMyClassrooms(String userId) {
        return classroomMemberMapper.selectClassroomsByUserId(userId);
    }

    @Override
    public PageResponse<ClassroomGradeDto> retrieveGradeList(Long classSn, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<ClassroomGradeDto> items = classroomMemberMapper.selectGradeList(classSn, offset, pageSize);
        int totalCount = classroomMemberMapper.countGradeList(classSn);
        return new PageResponse<>(items, totalCount);
    }

    @Override
    public PageResponse<kr.or.ddit.finalProject.dto.classroom.ClassroomMemberListResponse> retrieveMemberListPaged(Long classSn, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<kr.or.ddit.finalProject.dto.classroom.ClassroomMemberListResponse> items =
                classroomMemberMapper.selectMembersByClassSn(classSn, offset, pageSize);
        Map<String, Double> progressRates = retrieveProgressRates(classSn);
        items.forEach(m -> m.setProgressRate(progressRates.getOrDefault(m.getUserId(), 0.0)));
        int totalCount = classroomMemberMapper.countMembersByClassSn(classSn);
        return new PageResponse<>(items, totalCount);
    }

    @Override
    public kr.or.ddit.finalProject.dto.classroom.StudentDetailDto retrieveStudentDetail(Long classSn, String userId) {
        return classroomMemberMapper.selectStudentDetail(classSn, userId);
    }

    @Override
    public Map<String, Double> retrieveProgressRates(Long classSn) {
        return classroomMemberMapper.selectProgressRatesByClassSn(classSn)
                .stream().collect(Collectors.toMap(
                        ClassroomMemberListResponse::getUserId,
                        ClassroomMemberListResponse::getProgressRate));
    }

    @Override
    public List<ClassroomLectureResponse> retrieveLecturesWithProgress(Long classSn) {
        return lectureMapper.selectLecturesWithProgress(classSn);
    }

    @Override
    public List<WeeklyDayDto> retrieveWeeklyData(Long classSn) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd   = today.with(DayOfWeek.SUNDAY);

        List<Map<String, Object>> rows = classroomMapper.selectWeeklyCompletions(
                classSn, weekStart.format(YMD), weekEnd.format(YMD));

        Map<String, Integer> countByDate = rows.stream().collect(Collectors.toMap(
                r -> (String) r.get("DAY_DATE"),
                r -> ((Number) r.get("CNT")).intValue()
        ));

        int[] counts = new int[7];
        for (int i = 0; i < 7; i++) {
            counts[i] = countByDate.getOrDefault(weekStart.plusDays(i).format(YMD), 0);
        }

        int max = 0;
        for (int c : counts) max = Math.max(max, c);

        List<WeeklyDayDto> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            boolean empty  = counts[i] == 0;
            double height  = empty ? 4.0 : (max == 0 ? 4.0 : Math.round((double) counts[i] / max * 90) + 10.0);
            boolean isMax  = !empty && counts[i] == max;
            result.add(new WeeklyDayDto(DAY_LABELS[i], height, isMax, empty));
        }
        return result;
    }

    @Override
    public String retrieveWeeklyCompareText(Long classSn) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart     = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd       = today.with(DayOfWeek.SUNDAY);
        LocalDate lastWeekStart = weekStart.minusWeeks(1);
        LocalDate lastWeekEnd   = weekEnd.minusWeeks(1);

        int thisWeek = classroomMapper.selectTotalCompletionsInRange(
                classSn, weekStart.format(YMD), weekEnd.format(YMD));
        int lastWeek = classroomMapper.selectTotalCompletionsInRange(
                classSn, lastWeekStart.format(YMD), lastWeekEnd.format(YMD));

        int diff = thisWeek - lastWeek;
        if (diff == 0) return "지난주와 동일하게";
        return diff > 0 ? diff + "강 더" : Math.abs(diff) + "강 적게";
    }

    @Override
    public List<AchievementDto> retrieveAchievements(Long classSn) {
        int lecturePct    = toInt(classroomMapper.selectAvgProgressRate(classSn));
        int assignmentPct = toInt(classroomMapper.selectAssignmentSubmitRate(classSn));
        int examPct       = toInt(classroomMapper.selectExamTakerRate(classSn));

        return List.of(
                new AchievementDto("강의 수강", lecturePct,    "#3b82f6", offset(lecturePct)),
                new AchievementDto("과제 제출", assignmentPct, "#22c55e", offset(assignmentPct)),
                new AchievementDto("시험 응시", examPct,       "#f59e0b", offset(examPct))
        );
    }

    @Override
    public List<CalendarDayDto> retrieveCalendarDays(Long classSn, int year, int month) {
        List<String> eventDates = classroomMapper.selectEventDatesThisMonth(classSn, year, month);
        Set<String> eventSet = Set.copyOf(eventDates);

        LocalDate today = LocalDate.now();
        LocalDate first = LocalDate.of(year, month, 1);
        int daysInMonth = first.lengthOfMonth();

        List<CalendarDayDto> days = new ArrayList<>();
        for (int d = 1; d <= daysInMonth; d++) {
            LocalDate date   = LocalDate.of(year, month, d);
            boolean isToday  = date.equals(today);
            boolean hasEvent = eventSet.contains(date.format(YMD));
            days.add(new CalendarDayDto(d, isToday, hasEvent));
        }
        return days;
    }

    @Override
    public List<String> retrieveCalendarPadding(int year, int month) {
        LocalDate first = LocalDate.of(year, month, 1);
        int paddingCount = first.getDayOfWeek().getValue() % 7;
        return Collections.nCopies(paddingCount, "");
    }

    @Override
    public int retrieveUpcomingAssignmentCount(Long classSn) {
        return classroomMapper.selectUpcomingAssignmentCount(classSn);
    }

    @Override
    public TodayQuestionDto retrieveTodayQuestion(Long classSn) {
        Map<String, Object> row = classroomMapper.selectTodayExam(classSn);
        if (row == null) return null;
        return new TodayQuestionDto(
                (String) row.get("courseNm"),
                (String) row.get("examRegNm")
        );
    }

    @Override
    public int retrieveInactiveStudentCount(Long classSn) {
        LocalDate today = LocalDate.now();
        String weekStart = today.with(DayOfWeek.MONDAY).format(YMD);
        String weekEnd   = today.with(DayOfWeek.SUNDAY).format(YMD);
        return classroomMapper.selectInactiveStudentCount(classSn, weekStart, weekEnd);
    }

    private String formatYmd(String ymd) {
        return ymd.substring(0, 4) + "." + ymd.substring(4, 6) + "." + ymd.substring(6, 8);
    }

    private int toInt(Double value) {
        return value == null ? 0 : (int) Math.round(value);
    }

    private double offset(int pct) {
        return Math.round(CIRCUMFERENCE * (1.0 - pct / 100.0) * 100) / 100.0;
    }

    @Override
    @Transactional
    public void createClassroom(ClassroomDto dto) {
        classroomMapper.insertClassroom(dto);
    }

    /**
     * 클래스룸 상태 즉시 변경.
     * Mapper의 updateClassroomStatus 가 반환하는 업데이트 행 수로 성공 여부를 판단한다.
     * 0건이면 classSn 이 존재하지 않거나 instrUserId 가 담당자가 아닌 경우이다.
     */
    @Override
    @Transactional
    public boolean updateClassroomStatus(Long classSn, String classStatCd, String instrUserId) {
        int updated = classroomMapper.updateClassroomStatus(classSn, classStatCd, instrUserId, instrUserId);
        return updated > 0;
    }

    @Override
    public PageResponse<ClassroomListResponse> retrieveClassroomListPaged(String instrUserId, int page, int screenSize) {
        int offset = (page - 1) * screenSize;
        List<ClassroomListResponse> items = classroomMapper.selectClassroomListByInstructorPaged(instrUserId, offset, screenSize);
        // regDt 가 null 인 레거시 행 방어
        items.forEach(item -> {
            if (item.getRegDt() != null) {
                item.setFormattedRegDt(item.getRegDt().format(REG_DT_FORMAT));
            }
        });
        int totalCount = classroomMapper.countClassroomListByInstructor(instrUserId);
        return new PageResponse<>(items, totalCount);
    }
}
