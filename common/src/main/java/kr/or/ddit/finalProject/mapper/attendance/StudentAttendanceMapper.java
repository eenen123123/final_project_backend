package kr.or.ddit.finalProject.mapper.attendance;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.attendance.AttendanceRowDto;
import kr.or.ddit.finalProject.dto.attendance.AttendanceUpsertDto;
import kr.or.ddit.finalProject.dto.attendance.MyAttendanceDto;

@Mapper
public interface StudentAttendanceMapper {

    /** 특정 날짜의 클래스룸 수강생 전원 출결 현황 조회 */
    List<AttendanceRowDto> selectAttendanceByClassSnAndDate(
            @Param("classSn") Long classSn,
            @Param("date") String date);

    /** 학생 1명의 특정 날짜 출결 upsert */
    void upsertAttendance(AttendanceUpsertDto dto);

    /** 학생 본인의 클래스룸 출석 이력 전체 조회 */
    List<MyAttendanceDto> selectMyAttendanceByClassSn(
            @Param("classSn") Long classSn,
            @Param("userId") String userId);
}
