package kr.or.ddit.finalProject.mapper.classroom;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ClassroomHomeMapper {

    /** 특정 주(weekStart~weekEnd) 동안 강의 완료 건수를 날짜별로 반환. key: DAY_DATE(YYYYMMDD), CNT */
    List<Map<String, Object>> selectWeeklyCompletions(
            @Param("classSn") Long classSn,
            @Param("weekStart") String weekStart,
            @Param("weekEnd") String weekEnd);

    /** 특정 기간 전체 완료 건수 (주간 비교용) */
    int selectTotalCompletionsInRange(
            @Param("classSn") Long classSn,
            @Param("rangeStart") String rangeStart,
            @Param("rangeEnd") String rangeEnd);

    /** 강의 수강률(%) — LearningOverview avgProgressRate 로직을 단일 클래스로 축소 */
    Double selectAvgProgressRate(@Param("classSn") Long classSn);

    /** 과제 제출률(%) — 제출 건수 / (과제 수 × 활성 수강생) */
    Double selectAssignmentSubmitRate(@Param("classSn") Long classSn);

    /** 시험 응시률(%) — 가장 최근 시험 기준 EXAM_TAKER / 활성 수강생 */
    Double selectExamTakerRate(@Param("classSn") Long classSn);

    /** 이번 달 이벤트 날짜 목록(YYYYMMDD) — 시험 시작일 + 과제 마감일 합산 */
    List<String> selectEventDatesThisMonth(
            @Param("classSn") Long classSn,
            @Param("year") int year,
            @Param("month") int month);

    /** 마감일이 미래인 과제 수 */
    int selectUpcomingAssignmentCount(@Param("classSn") Long classSn);

    /** 오늘 진행 중인 시험 (EXAM_STRT_DT ≤ 오늘 ≤ EXAM_END_DT). 없으면 null */
    Map<String, Object> selectTodayExam(@Param("classSn") Long classSn);
}
