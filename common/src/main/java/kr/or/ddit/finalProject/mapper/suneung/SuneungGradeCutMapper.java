package kr.or.ddit.finalProject.mapper.suneung;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import kr.or.ddit.finalProject.dto.suneung.SuneungGradeCutDto;

@Mapper
public interface SuneungGradeCutMapper {

    List<SuneungGradeCutDto> selectSuneungGradeCutListByYear(int year);

    // 과목 목록 조회 (수능/모의평가 공통)
    List<String> selectSubjectList();
}
