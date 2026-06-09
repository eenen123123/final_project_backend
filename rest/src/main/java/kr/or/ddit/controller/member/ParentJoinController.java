package kr.or.ddit.controller.member;

import org.springframework.web.bind.annotation.RestController;
import kr.or.ddit.finalProject.dto.student.StudentInfoDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.service.member.ParentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@Slf4j
@RestController
@RequestMapping("/api/parent/join")
@RequiredArgsConstructor
public class ParentJoinController {

    private final ParentService parentService;


    @PostMapping("/info")
    public ResponseEntity<StudentInfoDto> getStudentInfo(@RequestBody Map<String, Object> body) {

        String joinLink = (String) body.get("joinLink");
        if (joinLink == null) {
            throw new FinalProjectException(ErrorCode.INVALID_JOIN_LINK);
        }

        // 토큰 검증
        parentService.validateJoinLink(joinLink);

        // 학생 정보 조회
        StudentInfoDto studentInfo =
                new StudentInfoDto(parentService.getStudentInfoByJoinLink(joinLink));

        return ResponseEntity.ok(studentInfo);

    }

    @PostMapping
    public ResponseEntity<Void> joinParent(Authentication authentication,
            @RequestBody Map<String, Object> body) {

        String joinLink = (String) body.get("joinLink");
        String studentId = (String) body.get("studentId");

        if (joinLink == null || studentId == null) {
            throw new FinalProjectException(ErrorCode.INVALID_JOIN_LINK);
        }

        parentService.joinParent(authentication, joinLink, studentId);

        return ResponseEntity.ok().build();
    }

}
