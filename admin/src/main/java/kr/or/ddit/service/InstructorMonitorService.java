package kr.or.ddit.service;

import java.util.List;

import kr.or.ddit.finalProject.dto.monitoring.ClassroomOverviewDto;
import kr.or.ddit.finalProject.dto.monitoring.InstructorMonitorCardDto;

public interface InstructorMonitorService {

    List<InstructorMonitorCardDto> getInstructorCards();

    int getActiveClassCount();

    int getTotalStudentCount();

    int getThisMonthJournalCount();

    List<ClassroomOverviewDto> getActiveClassrooms();
}
