package kr.or.ddit.controller.instructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.instructor.InstructorListResponse;
import kr.or.ddit.finalProject.mapper.instructor.InstructorMapper;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/instructors")
@RequiredArgsConstructor
public class InstructorController {

    private final InstructorMapper instructorMapper;

    // GET /api/instructors/by-subject
    // 과목 분류별 강사 목록 반환
    // 응답 예: { "프로그래밍": [{instrUserId, userName, instrProfileImg}, ...], ... }
    @GetMapping("/by-subject")
    public ResponseEntity<Map<String, List<InstructorListResponse>>> getInstructorsBySubject() {
        List<InstructorListResponse> flat = instructorMapper.selectInstructorsBySubject();

        Map<String, List<InstructorListResponse>> grouped = flat.stream()
                .collect(Collectors.groupingBy(
                        InstructorListResponse::getSubjectClNm,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return ResponseEntity.ok(grouped);
    }

}
