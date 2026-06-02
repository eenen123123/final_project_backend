package kr.or.ddit.controller.classroom;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.service.classroom.ClassroomService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/classroom")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

}
