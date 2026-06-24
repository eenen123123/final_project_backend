package kr.or.ddit.finalProject.mapper.classroom;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse;
import kr.or.ddit.finalProject.dto.classroom.ClassroomDto;
import kr.or.ddit.finalProject.dto.classroom.ClassroomListResponse;

@Mapper
public interface ClassroomMapper {

    // 강사 담당 클래스룸 목록 (강좌명·상태·수강생수 포함)
    List<ClassroomListResponse> selectClassroomListByInstructor(@Param("instrUserId") String instrUserId);

    // 클래스룸 단건 상세
    ClassroomDetailResponse selectClassroomBySn(@Param("classSn") Long classSn);

    // 지정 기간(weekStart~weekEnd) 내 일별 강의 완료 건수
    List<Map<String, Object>> selectWeeklyCompletions(
            @Param("classSn") Long classSn,
            @Param("weekStart") String weekStart,
            @Param("weekEnd") String weekEnd);

    // 지정 기간 내 강의 완료 건수 합계 (전주/이번주 비교용)
    int selectTotalCompletionsInRange(
            @Param("classSn") Long classSn,
            @Param("rangeStart") String rangeStart,
            @Param("rangeEnd") String rangeEnd);

    // 수강생 전체 평균 강의 진도율 (0~100, 없으면 null)
    Double selectAvgProgressRate(@Param("classSn") Long classSn);

    // 과제 전체 제출률 (제출 인원 / 수강생 수, 없으면 null)
    Double selectAssignmentSubmitRate(@Param("classSn") Long classSn);

    // 시험 응시율 (응시 인원 / 수강생 수, 없으면 null)
    Double selectExamTakerRate(@Param("classSn") Long classSn);

    // 해당 월에 학습 이벤트가 있는 날짜 목록 (YYYY-MM-DD, 캘린더 점 표시용)
    List<String> selectEventDatesThisMonth(
            @Param("classSn") Long classSn,
            @Param("year") int year,
            @Param("month") int month);

    // 마감이 임박한 과제 수 (탭 뱃지용)
    int selectUpcomingAssignmentCount(@Param("classSn") Long classSn);

    // 오늘 진행되는 시험 정보 (없으면 null)
    Map<String, Object> selectTodayExam(@Param("classSn") Long classSn);

    // 지정 기간 내 학습 기록이 없는 비활성 수강생 수
    int selectInactiveStudentCount(
            @Param("classSn") Long classSn,
            @Param("weekStart") String weekStart,
            @Param("weekEnd") String weekEnd);

    // 클래스룸 신규 등록
    void insertClassroom(ClassroomDto dto);

    /**
     * 클래스룸 상태 변경.
     * WHERE 절에 EXISTS(COURSE.INSTR_USER_ID)를 포함해 소유권을 함께 검증한다.
     * 본인 클래스룸이 아니면 UPDATE 0건 → 반환값 0으로 구분.
     *
     * @return 실제로 업데이트된 행 수 (1 = 성공, 0 = 권한 없음 또는 존재하지 않음)
     */
    int updateClassroomStatus(
            @Param("classSn")     Long   classSn,
            @Param("classStatCd") String classStatCd,
            @Param("instrUserId") String instrUserId,
            @Param("lastMdfrId")  String lastMdfrId);

    // 페이징 목록
    List<ClassroomListResponse> selectClassroomListByInstructorPaged(
            @Param("instrUserId") String instrUserId,
            @Param("offset") int offset,
            @Param("screenSize") int screenSize);

    // 전체 건수 (페이지 계산용)
    int countClassroomListByInstructor(@Param("instrUserId") String instrUserId);
}
