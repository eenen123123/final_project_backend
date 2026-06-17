package kr.or.ddit.finalProject.service.suneung;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import kr.or.ddit.finalProject.dto.suneung.ExamType;
import kr.or.ddit.finalProject.dto.suneung.SuneungGradeCutDto;
import kr.or.ddit.finalProject.mapper.suneung.SuneungGradeCutMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuneungService {
    private final SuneungGradeCutMapper suneungGradeCutMapper;

    public Map<String, ?> getSubjectsAndYears() {
        // subjClNm을 기준으로 과목을 그룹화하여 조회
        List<SuneungGradeCutDto> subjects = suneungGradeCutMapper.selectSubjectList();
        log.info("subjects: {}", subjects);
        var groupedSubjects = subjects.stream().collect(Collectors.groupingBy(SuneungGradeCutDto::getSubjClNm));

        List<Integer> years = suneungGradeCutMapper.selectYearList();
        return Map.of("subjects", groupedSubjects, "years", years);
    }

    public List<SuneungGradeCutDto> getGradeCuts(int year, String subject, ExamType examType) {
        return suneungGradeCutMapper.selectSuneungGradeCutListByYearAndSubject(year, subject, examType);
    }

    public List<Integer> getYears() {
        return suneungGradeCutMapper.selectYearList();
    }

    public List<ExamType> getExamTypes(Integer year) {
        List<ExamType> gradeCuts = suneungGradeCutMapper.selectExamTypesByYear(year);
        return gradeCuts;
    }

    public List<String> getSubjectClassifications(Integer year, ExamType examType) {
        List<SuneungGradeCutDto> subjects = suneungGradeCutMapper.selectSubjectListByYearAndExamType(year, examType);
        return subjects.stream()
                .map(SuneungGradeCutDto::getSubjClNm)
                .distinct()
                .collect(Collectors.toList());
    }
}
