package kr.or.ddit.controller.suneung;

import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.suneung.ExamType;
import kr.or.ddit.finalProject.dto.suneung.SuneungGradeCutDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.service.suneung.SuneungService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RestController
@RequestMapping("/api/suneung")
@RequiredArgsConstructor
public class SuneungController {
    private final SuneungService suneungService;

    @GetMapping("/years")
    public ResponseEntity<List<Integer>> getYears() {
        List<Integer> years = suneungService.getYears();
        return ResponseEntity.ok(years);
    }

    @GetMapping("/exam-types")
    public ResponseEntity<List<ExamType>> getExamTypes(@RequestParam Integer year) {
        List<ExamType> examTypes = suneungService.getExamTypes(year);
        return ResponseEntity.ok(examTypes);
    }

    @GetMapping("/subject-classifications")
    public ResponseEntity<List<String>> getSubjectClassifications(@RequestParam Integer year,
            @RequestParam String examType) {
        ExamType examTypeEnum;
        try {
            examTypeEnum = ExamType.valueOf(examType);
        } catch (IllegalArgumentException e) {
            throw new FinalProjectException(ErrorCode.NOT_SUPPORTED_EXAM_TYPE);
        }

        List<String> subjectClassifications = suneungService.getSubjectClassifications(year, examTypeEnum);
        return ResponseEntity.ok(subjectClassifications);
    }

    @GetMapping("/subjects")
    public ResponseEntity<Map<String, List<String>>> getSubjects(@RequestParam Integer year,
            @RequestParam String examType) {
        ExamType examTypeEnum;
        try {
            examTypeEnum = ExamType.valueOf(examType);
        } catch (IllegalArgumentException e) {
            throw new FinalProjectException(ErrorCode.NOT_SUPPORTED_EXAM_TYPE);
        }

        Map<String, List<String>> subjects = suneungService.getSubjects(year, examTypeEnum);
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/subjects-and-years")
    public ResponseEntity<Map<String, ?>> getSubjectsAndYears() {
        Map<String, ?> subjectsAndYears = suneungService.getSubjectsAndYears();
        return ResponseEntity.ok(subjectsAndYears);
    }

    @GetMapping("/grade-cuts")
    public ResponseEntity<List<SuneungGradeCutDto>> getGradeCuts(@RequestParam Integer year,
            @RequestParam String subject,
            @RequestParam String examType) {

        ExamType examTypeEnum;
        try {
            examTypeEnum = ExamType.valueOf(examType);
        } catch (IllegalArgumentException e) {
            throw new FinalProjectException(ErrorCode.NOT_SUPPORTED_EXAM_TYPE);
        }
        List<SuneungGradeCutDto> gradeCuts = suneungService.getGradeCuts(year, subject, examTypeEnum);
        return ResponseEntity.ok(gradeCuts);
    }

}
