package kr.or.ddit.controller.classroom;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.classroom.ClassroomDto;
import kr.or.ddit.finalProject.dto.classroom.ClassroomMemberDto;
import kr.or.ddit.finalProject.service.classroom.ClassroomService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/classroom")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    // GET /api/classroom/{classId}
    @GetMapping("/{classId}")
    public ResponseEntity<ClassroomDto> getClassroom(@PathVariable Long classId) {
        return ResponseEntity.ok(classroomService.retrieveClassroom(classId));
    }

    // GET /api/classroom/{classId}/members?all=true (강사 전용)
    @GetMapping("/{classId}/members")
    public ResponseEntity<List<ClassroomMemberDto>> getMembers(
            @PathVariable Long classId,
            @RequestParam(defaultValue = "false") boolean all,
            Authentication authentication) {
        if (all && (authentication == null || authentication.getName() == null)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String requesterId = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(classroomService.retrieveMembers(classId, all, requesterId));
    }
}
