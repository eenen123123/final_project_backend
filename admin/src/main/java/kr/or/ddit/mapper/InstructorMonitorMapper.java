package kr.or.ddit.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.monitoring.ClassroomOverviewDto;
import kr.or.ddit.finalProject.dto.monitoring.InstructorMonitorCardDto;

@Mapper
public interface InstructorMonitorMapper {

    List<InstructorMonitorCardDto> selectInstructorCards();

    int selectActiveClassCount();

    int selectTotalStudentCount();

    int selectThisMonthJournalCount();

    int selectMyJournalCountThisMonth(@Param("userId") String userId);

    List<ClassroomOverviewDto> selectActiveClassrooms();
}
