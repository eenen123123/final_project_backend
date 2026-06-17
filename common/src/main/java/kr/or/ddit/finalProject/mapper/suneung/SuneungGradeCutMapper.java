package kr.or.ddit.finalProject.mapper.suneung;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.suneung.ExamType;
import kr.or.ddit.finalProject.dto.suneung.SuneungGradeCutDto;

@Mapper
public interface SuneungGradeCutMapper {

    List<SuneungGradeCutDto> selectSuneungGradeCutListByYear(int year);

    List<SuneungGradeCutDto> selectSuneungGradeCutListByYearAndSubject(int year, String subject, ExamType examType);

    List<Integer> selectYearList();

    // 과목 목록 조회 (수능/모의평가 공통)
    List<SuneungGradeCutDto> selectSubjectList();

    List<ExamType> selectExamTypesByYear(Integer year);

    List<SuneungGradeCutDto> selectSubjectListByYearAndExamType(@Param("year") Integer year,
            @Param("examType") ExamType examType);
}
