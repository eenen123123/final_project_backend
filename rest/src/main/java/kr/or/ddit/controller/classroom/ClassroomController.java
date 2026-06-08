package kr.or.ddit.controller.classroom;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse;
import kr.or.ddit.finalProject.dto.classroom.ClassroomListResponse;
import kr.or.ddit.finalProject.mapper.classroom.ClassroomMemberMapper;
import kr.or.ddit.finalProject.service.classroom.ClassroomService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/classroom")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;
    private final ClassroomMemberMapper classroomMemberMapper;

    @GetMapping("/my")
    public ResponseEntity<List<ClassroomListResponse>> getMyClassrooms(Authentication authentication) {
        return ResponseEntity.ok(classroomMemberMapper.selectClassroomsByUserId(authentication.getName()));
    }

    @GetMapping("/{classSn}")
    public ResponseEntity<ClassroomDetailResponse> getClassroomDetail(@PathVariable Long classSn) {
        return ResponseEntity.ok(classroomService.retrieveClassroomDetail(classSn));
    }

}
