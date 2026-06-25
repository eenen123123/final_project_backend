package kr.or.ddit.controller.member;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import kr.or.ddit.finalProject.dto.enrollment.CourseEnrollmentDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.lecture.LectureMapper;
import kr.or.ddit.finalProject.service.enrollment.CourseEnrollmentService;
import kr.or.ddit.finalProject.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class RestMyPageController {

    private final MemberService memberService;
    private final CourseEnrollmentService enrollmentService;
    private final LectureMapper lectureMapper;

    /**
     * 비밀번호 확인 메서드 개인정보 수정 페이지 진입 전 본인 확인을 위해 현재 비밀번호를 검증한다.
     *
     * @param authHeader Authorization 헤더 (Bearer 토큰)
     * @param body { "password": "현재 비밀번호" }
     * @return 비밀번호 일치 여부 (true)
     */
    // GET /api/mypage/subject-progress - 과목별 시청 시간 (레이더 차트용)
    @GetMapping("/subject-progress")
    public ResponseEntity<List<Map<String, Object>>> getSubjectProgress(Authentication authentication) {
        return ResponseEntity.ok(lectureMapper.selectSubjectProgress(authentication.getName()));
    }

    @GetMapping("/courses")
    public ResponseEntity<List<CourseEnrollmentDto>> getMyCourses(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(enrollmentService.getMyEnrolledCourses(userId));
    }

    @PostMapping("/verify-password")
    public ResponseEntity<Boolean> verifyPassword(Authentication authentication,
            @RequestBody Map<String, String> body) {

        String userId = authentication.getName();
        String password = body.get("password");

        boolean isValid = memberService.verifyPassword(userId, password);
        if (!isValid) {
            throw new FinalProjectException(ErrorCode.USERNAME_OR_PASSWORD_INCORRECT);
        }

        return ResponseEntity.ok(true);
    }

    /**
     * 프로필 조회 메서드 현재 로그인한 사용자의 개인정보를 조회한다.
     *
     * @param authHeader Authorization 헤더 (Bearer 토큰)
     * @return 사용자 정보를 담은 MemberDto 객체
     */
    @GetMapping("/profile")
    public ResponseEntity<MemberDto> getProfile(Authentication authentication) {

        MemberDto memberDto = memberService.getMemberByUserId(authentication.getName());

        memberDto.setUserEnpswd(null);
        memberDto.setUserEnrrno(null);

        return ResponseEntity.ok(memberDto);
    }

    /**
     * 프로필 수정 메서드 현재 로그인한 사용자의 개인정보를 수정한다. 비밀번호는 입력한 경우에만 변경되며, 프로필 이미지는 선택적으로 업로드할 수 있다.
     *
     * @param authHeader Authorization 헤더 (Bearer 토큰)
     * @param userTelno 전화번호
     * @param userEmailAddr 이메일
     * @param userZip 우편번호
     * @param userAddr 주소
     * @param userDaddr 상세주소
     * @param newPassword 새 비밀번호 (선택)
     * @param profileImage 프로필 이미지 파일 (선택)
     * @return 수정 성공 여부 (true)
     */
    @PutMapping("/profile")
    public ResponseEntity<Boolean> updateProfile(Authentication authentication,
            @RequestPart(value = "userTelno", required = false) String userTelno,
            @RequestPart(value = "userEmailAddr", required = false) String userEmailAddr,
            @RequestPart(value = "userZip", required = false) String userZip,
            @RequestPart(value = "userAddr", required = false) String userAddr,
            @RequestPart(value = "userDaddr", required = false) String userDaddr,
            @RequestPart(value = "newPassword", required = false) String newPassword,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        MemberDto memberDto = memberService.getMemberByUserId(authentication.getName());

        // 비밀번호 변경 요청 시 암호화 후 세팅
        if (newPassword != null && !newPassword.isBlank()) {
            // TODO: PasswordEncoder 주입 후 암호화 처리
            memberDto.setUserEnpswd(newPassword);
            log.info("비밀번호 변경 요청 - userId: {}", memberDto.getUserId());
        }

        // 프로필 이미지 업로드 처리
        if (profileImage != null && !profileImage.isEmpty()) {
            // TODO: 파일 업로드 서비스 연동 후 URL 세팅
            // String imageUrl = fileUploadService.upload(profileImage);
            // memberDto.setUserProfile(imageUrl);
            log.info("프로필 이미지 업로드 요청 - userId: {}", memberDto.getUserId());
        }

        memberService.updateMember(memberDto);

        return ResponseEntity.ok(true);
    }

    // DELETE /api/mypage/withdraw - 회원 탈퇴 (소프트 딜리트)
    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw(Authentication authentication,
            @RequestBody(required = false) Map<String, String> body) {
        String userId = authentication.getName();
        String reason = body != null ? body.getOrDefault("reason", "") : "";
        memberService.withdrawMember(userId, reason);
        return ResponseEntity.noContent().build();
    }
}
