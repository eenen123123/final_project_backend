package kr.or.ddit.finalProject.mapper.classroom;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse;
import kr.or.ddit.finalProject.dto.classroom.ClassroomListResponse;

@Mapper
public interface ClassroomMapper {

    List<ClassroomListResponse> selectClassroomListByInstructor(@Param("instrUserId") String instrUserId);

    ClassroomDetailResponse selectClassroomBySn(@Param("classSn") Long classSn);

    List<Map<String, Object>> selectWeeklyCompletions(
            @Param("classSn") Long classSn,
            @Param("weekStart") String weekStart,
            @Param("weekEnd") String weekEnd);

    int selectTotalCompletionsInRange(
            @Param("classSn") Long classSn,
            @Param("rangeStart") String rangeStart,
            @Param("rangeEnd") String rangeEnd);

    Double selectAvgProgressRate(@Param("classSn") Long classSn);

    Double selectAssignmentSubmitRate(@Param("classSn") Long classSn);

    Double selectExamTakerRate(@Param("classSn") Long classSn);

    List<String> selectEventDatesThisMonth(
            @Param("classSn") Long classSn,
            @Param("year") int year,
            @Param("month") int month);

    int selectUpcomingAssignmentCount(@Param("classSn") Long classSn);

    Map<String, Object> selectTodayExam(@Param("classSn") Long classSn);

    int selectInactiveStudentCount(
            @Param("classSn") Long classSn,
            @Param("weekStart") String weekStart,
            @Param("weekEnd") String weekEnd);
}
