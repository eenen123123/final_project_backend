package kr.or.ddit.finalProject.mapper.attendance;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.attendance.AttendanceHistoryDto;
import kr.or.ddit.finalProject.dto.attendance.ClassAttendanceSummaryDto;

@Mapper
public interface StudentAttendanceMapper {

    /** 클래스룸 수강생별 근태 특이사항(결석/지각/조퇴) 집계 */
    List<ClassAttendanceSummaryDto> selectAttendanceSummaryByClassSn(
            @Param("classSn") Long classSn);

    /** 학생 1명의 근태 특이사항(결석/지각/조퇴) 전체 이력 */
    List<AttendanceHistoryDto> selectAttendanceHistoryByStudent(
            @Param("userId") String userId);
}
