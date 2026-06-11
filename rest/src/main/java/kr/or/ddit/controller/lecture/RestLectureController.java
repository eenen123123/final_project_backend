package kr.or.ddit.controller.lecture;

import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.lecture.LectureDto;
import kr.or.ddit.finalProject.dto.lecture.LectureProgressUpdateRequest;
import kr.or.ddit.finalProject.dto.lecture.LectureResponseDto;
import kr.or.ddit.finalProject.service.lecture.LectureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RestController
@RequestMapping("/api/lecture")
@RequiredArgsConstructor
public class RestLectureController {
    private final LectureService lectureService;

    @GetMapping("/info")
    public ResponseEntity<LectureDto> getLectureInfo(@RequestParam Long lectureId, @RequestParam Long courseId,
            Authentication authentication) {
        // 강의 정보를 조회하고, 해당 사용자가 접근 가능한 강의인지 확인

        // 했다 치고 해당 강의의 정보를 반환

        LectureDto lectureDto = lectureService.retrieveLectureBySn(lectureId);

        return ResponseEntity.ok(lectureDto);
    }

    // 강의 진행 상황 업데이트 엔드포인트
    @PostMapping("/progress")
    public ResponseEntity<Void> setLectureProgress(@RequestBody LectureProgressUpdateRequest request,
            Authentication authentication) {
        // 강의 진행 상황 업데이트 로직을 호출
        lectureService.updateLectureProgress(request.lectureId(), request.courseId(), request.progress(),
                authentication.getName());

        log.info("Updated lecture progress: lectureId={}, courseId={}, progress={}, userId={}",
                request.lectureId(), request.courseId(), request.progress(), authentication.getName());

        return ResponseEntity.ok().build();
    }

}
