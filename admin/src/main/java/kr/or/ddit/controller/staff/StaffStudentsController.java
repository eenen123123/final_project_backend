package kr.or.ddit.controller.staff;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import kr.or.ddit.finalProject.dto.member.MemberCreateLogDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.service.file.CloudinaryUploadService;
import kr.or.ddit.finalProject.service.staff.StaffService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;




@Slf4j
@Controller
@RequestMapping("/admin")
public class StaffStudentsController {

    @Autowired
    StaffService staffService;

    @Autowired
    CloudinaryUploadService cloudinaryUploadService;

    /**
     * 학생 관리 메인 화면 이동 및 초기 데이터 조회
     * 
     * ✔ 학생 정보 및 계정 관리 페이지(인사 관리 대시보드)를
     * 요청할 때 진입하는 컨트롤러 메서드
     * 
     * ✔ 역할 요약
     * ---------------------------------------------------------------------
     * - 전체 학생 목록 데이터 확보
     * - 검색 및 필터링용 메타데이터(학과, 학년, 입학 연도) 조회
     * - 뷰(View) 템플릿으로 데이터 전달 및 포워딩
     * 
     * 
     */

    @GetMapping("/employees/students")
    public String getStudents(Model model) {
        log.info("getStudents");

        // 1. 학생 관리 대시보드 테이블에 노출할 전체 학생 상세 목록 데이터를 조회한다.
        List<MemberDto> studentList = staffService.retrieveStudentList();

        // 2. 가입 연도별 필터링 기능을 지원하기 위해 시스템에 등록된 전체 가입 연도 목록을 조회한다.
        List<Integer> joinYearList = staffService.retrieveMemberJoinYearList();
        
        model.addAttribute("studentList", studentList);
        model.addAttribute("joinYearList", joinYearList);

        return "admin:/staff/students";
    }

    /**
     * 학생 ID 자동 순번 발급 (예: 26S00001)
     */
    @GetMapping("/employees/students/next-id")
    @ResponseBody
    public ResponseEntity<String> getNextStudentId(
            @RequestParam String baseId,
            @RequestParam String defaultSerial) {
        String nextId = staffService.getNextAvailableId(baseId, defaultSerial);
        return ResponseEntity.ok(nextId);
    }

    /**
     * 신규 회원 등록 처리
     */
    @PostMapping("/employees/students")
    public String createStudent(
        MemberDto memberDto,
        MemberCreateLogDto memberCreateLog,
        @RequestParam(required = false) MultipartFile profileImage,
        Principal principal
    ) {
        // 로그인한 관리자의 ID를 꺼냄
        String loginAdmin = "SYSTEM"; // 기본값

        if (principal != null) {
            loginAdmin = principal.getName(); // 세션이나 토큰에 저장된 로그인 ID
        }

        // 프로필 사진 cloudinary 업로드
        String profileUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                profileUrl = cloudinaryUploadService.uploadFileToCloudinary(profileImage);
            } catch (Exception e) {
                log.error("[createStudent] Cloudinary 업로드 실패: {}", e.getMessage());
                return "redirect:/admin/employees/students?error=" + URLEncoder.encode("프로필 이미지 업로드에 실패했습니다.", StandardCharsets.UTF_8);
            }
        }

        try {
            staffService.registerStudent(memberDto, memberCreateLog, profileUrl, loginAdmin);            
        } catch (FinalProjectException e) {
            log.warn("[createStudent] 등록 실패: {}", e.getMessage());
            return "redirect:/admin/employees/students?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            log.warn("[createStudent] 유효성 검사 실패: {}", e.getMessage());
            return "redirect:/admin/employees/students?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }

        return "redirect:/admin/employees?success=" + URLEncoder.encode("회원이 성공적으로 등록되었습니다.", StandardCharsets.UTF_8);
    }
    
    /**
     * 학생 정보 수정
     */
    @PutMapping("/students/update")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateStudent(
            @RequestParam String userId,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String userTelno,
            @RequestParam(required = false) String userEmailAddr,
            @RequestParam(required = false) String userZip,
            @RequestParam(required = false) String userAddr,
            @RequestParam(required = false) String userDaddr,
            @RequestParam(required = false) String userProfile,
            @RequestParam(required = false) String userRole,
            @RequestParam(required = false) String enable,
            @RequestParam(required = false) MultipartFile editProfileImage,
            Principal principal
    ) {
        String loginAdminId = principal != null ? principal.getName() : "SYSTEM";

        // 새 이미지가 있으면 Cloudinary 업로드, 없으면 기존 URL 유지
        String finalProfileUrl = userProfile;
        if (editProfileImage != null && !editProfileImage.isEmpty()) {
            try {
                finalProfileUrl = cloudinaryUploadService.uploadFileToCloudinary(editProfileImage);
            } catch (Exception e) {
                log.error("[updateStudent] Cloudinary 업로드 실패: {}", e.getMessage());
            }
        }

        MemberDto memberDto = new MemberDto();
        memberDto.setUserId(userId);
        memberDto.setUserName(userName);
        memberDto.setUserTelno(userTelno);
        memberDto.setUserEmailAddr(userEmailAddr);
        memberDto.setUserZip(userZip);
        memberDto.setUserAddr(userAddr);
        memberDto.setUserDaddr(userDaddr);
        memberDto.setUserProfile(finalProfileUrl);
        memberDto.setUserRole(userRole);
        memberDto.setEnable(enable);

        try {
            staffService.updateStudent(memberDto, loginAdminId);
            return ResponseEntity.ok(Map.of("result", "success",
                    "profileUrl", finalProfileUrl != null ? finalProfileUrl : ""));
        } catch (Exception e) {
            log.error("[updateStudent] 수정 실패. userId={}, cause={}", userId, e.getMessage());
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "error", "message", e.getMessage()));
        }
    }

    /**
     * 학생 탈퇴 처리
     */
    @PutMapping("/students/{userId}/retirement")
    @ResponseBody
    public ResponseEntity<Map<String, String>>retireStudent(
        @PathVariable String userId,
        @RequestBody Map<String, String> body,
        Principal principal
    ) {

        // 로그인한 관리자의 ID를 꺼냄
        String loginUserId = principal != null ? principal.getName() : "SYSTEM";

        // 요청 본문에서 탈퇴 사유 추출
        String withdrawRsn = body.get("withdrawRsn");

        // 서비스 호출하여 회원 탈퇴 처리
        try {
            staffService.retireStudent(userId, withdrawRsn, loginUserId);
            return ResponseEntity.ok(Map.of("result", "success"));
        } catch (FinalProjectException e) {
            log.warn("[retireStudent] 탈퇴 처리 실패. userId={}, cause={}", userId, e.getMessage());
            return ResponseEntity.status(e.getErrorCode().getStatus())
                                .body(Map.of("result", "error", "message", e.getMessage()));
        }
    }
    
}
