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
import kr.or.ddit.finalProject.dto.classroom.CalendarDayDto;
import kr.or.ddit.finalProject.dto.classroom.TodayQuestionDto;
import kr.or.ddit.finalProject.dto.classroom.WeeklyDayDto;
import kr.or.ddit.finalProject.mapper.classroom.ClassroomHomeMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassroomHomeServiceImpl implements ClassroomHomeService {

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final double CIRCUMFERENCE = 2 * Math.PI * 20; // SVG r=20 → 125.66
    private static final String[] DAY_LABELS = { "월", "화", "수", "목", "금", "토", "일" };

    private final ClassroomHomeMapper homeMapper;

    // ─────────────────────────────────────────────────────────────
    // 주간 바 차트
    // ─────────────────────────────────────────────────────────────

    @Override
    public List<WeeklyDayDto> retrieveWeeklyData(Long classSn) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd   = today.with(DayOfWeek.SUNDAY);

        List<Map<String, Object>> rows = homeMapper.selectWeeklyCompletions(
                classSn,
                weekStart.format(YMD),
                weekEnd.format(YMD)
        );

        // 날짜 → 완료 건수 맵
        Map<String, Integer> countByDate = rows.stream().collect(Collectors.toMap(
                r -> (String) r.get("DAY_DATE"),
                r -> ((Number) r.get("CNT")).intValue()
        ));

        // 월~일 순서로 7일 배열
        int[] counts = new int[7];
        for (int i = 0; i < 7; i++) {
            String key = weekStart.plusDays(i).format(YMD);
            counts[i] = countByDate.getOrDefault(key, 0);
        }

        int max = 0;
        for (int c : counts) max = Math.max(max, c);

        List<WeeklyDayDto> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            boolean empty = counts[i] == 0;
            double height = empty ? 4.0 : (max == 0 ? 4.0 : Math.round((double) counts[i] / max * 90) + 10.0);
            boolean isMax = !empty && counts[i] == max;
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

        int thisWeek = homeMapper.selectTotalCompletionsInRange(
                classSn, weekStart.format(YMD), weekEnd.format(YMD));
        int lastWeek = homeMapper.selectTotalCompletionsInRange(
                classSn, lastWeekStart.format(YMD), lastWeekEnd.format(YMD));

        int diff = thisWeek - lastWeek;
        if (diff == 0) return "지난주와 동일하게";
        return diff > 0 ? diff + "강 더" : Math.abs(diff) + "강 적게";
    }

    // ─────────────────────────────────────────────────────────────
    // 학습 달성률 도넛
    // ─────────────────────────────────────────────────────────────

    @Override
    public List<AchievementDto> retrieveAchievements(Long classSn) {
        int lecturePct    = toInt(homeMapper.selectAvgProgressRate(classSn));
        int assignmentPct = toInt(homeMapper.selectAssignmentSubmitRate(classSn));
        int examPct       = toInt(homeMapper.selectExamTakerRate(classSn));

        return List.of(
                new AchievementDto("강의 수강", lecturePct,    "#3b82f6", offset(lecturePct)),
                new AchievementDto("과제 제출", assignmentPct, "#22c55e", offset(assignmentPct)),
                new AchievementDto("시험 응시", examPct,       "#f59e0b", offset(examPct))
        );
    }

    // ─────────────────────────────────────────────────────────────
    // 미니 캘린더
    // ─────────────────────────────────────────────────────────────

    @Override
    public List<CalendarDayDto> retrieveCalendarDays(Long classSn, int year, int month) {
        List<String> eventDates = homeMapper.selectEventDatesThisMonth(classSn, year, month);
        Set<String> eventSet = Set.copyOf(eventDates);

        LocalDate today  = LocalDate.now();
        LocalDate first  = LocalDate.of(year, month, 1);
        int daysInMonth  = first.lengthOfMonth();

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
        // DayOfWeek.MONDAY=1 기준, 월요일이 첫 열. 일요일(7)은 7번째 열.
        int dayOfWeek = first.getDayOfWeek().getValue(); // 월=1, 일=7
        // 일요일 시작(0-indexed Sunday=0)으로 변환: 월=1, 화=2, ..., 일=0
        int paddingCount = (dayOfWeek % 7); // 월:1, 화:2, 수:3, 목:4, 금:5, 토:6, 일:0
        return Collections.nCopies(paddingCount, "");
    }

    // ─────────────────────────────────────────────────────────────
    // 기타
    // ─────────────────────────────────────────────────────────────

    @Override
    public int retrieveUpcomingAssignmentCount(Long classSn) {
        return homeMapper.selectUpcomingAssignmentCount(classSn);
    }

    @Override
    public TodayQuestionDto retrieveTodayQuestion(Long classSn) {
        Map<String, Object> row = homeMapper.selectTodayExam(classSn);
        if (row == null) return null;
        return new TodayQuestionDto(
                (String) row.get("COURSENM"),
                (String) row.get("EXAMREGNM")
        );
    }

    // ─────────────────────────────────────────────────────────────
    // 내부 유틸
    // ─────────────────────────────────────────────────────────────

    private int toInt(Double value) {
        return value == null ? 0 : (int) Math.round(value);
    }

    private double offset(int pct) {
        return Math.round(CIRCUMFERENCE * (1.0 - pct / 100.0) * 100) / 100.0;
    }
}
