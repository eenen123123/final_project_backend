package kr.or.ddit.controller.staff;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import kr.or.ddit.finalProject.dto.member.MemberCreateLogDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.dto.staff.AdminActivityType;
import kr.or.ddit.finalProject.service.file.CloudinaryUploadService;
import kr.or.ddit.finalProject.service.member.ParentService;
import kr.or.ddit.finalProject.service.staff.StaffService;
import kr.or.ddit.service.AdminActivityApprovalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.paging.PaginationInfo;



@Slf4j
@Controller
@RequestMapping("/admin")
public class StaffStudentsController {

    @Autowired
    StaffService staffService;

    @Autowired
    AdminActivityApprovalService activityApprovalService;


    @Autowired
    ParentService parentService;

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

    private static final int PAGE_SIZE = 10;
    private static final int BLOCK_SIZE = 5;

    /**
     * 학생 목록 동적 검색 + 서버 페이징 (AJAX)
     */
    @GetMapping("/employees/students/search")
    @ResponseBody
    public ResponseEntity<PageResponse<MemberDto>> searchStudents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String userRole,
            @RequestParam(required = false) String enable,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int screenSize,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String orderDirection) {
        Map<String, Object> params = new HashMap<>();
        if (keyword != null && !keyword.isBlank())
            params.put("keyword", keyword.trim());
        if (year != null && !year.isBlank())
            params.put("year", year.trim());
        if (userRole != null && !userRole.isBlank())
            params.put("userRole", userRole.trim());
        if (enable != null && !enable.isBlank())
            params.put("enable", enable.trim());

        String safeDir = "DESC".equalsIgnoreCase(orderDirection) ? "DESC" : "ASC";
        PaginationInfo<Map<String, Object>> paging =
                new PaginationInfo<>(screenSize, BLOCK_SIZE, page, orderBy, safeDir);
        paging.setDetailCondition(params);

        return ResponseEntity.ok(staffService.searchStudentList(paging));
    }

    /**
     * 학생 ID 자동 순번 발급 (예: 26S00001)
     */
    @GetMapping("/employees/students/next-id")
    @ResponseBody
    public ResponseEntity<String> getNextStudentId(@RequestParam String baseId,
            @RequestParam String defaultSerial) {
        String nextId = staffService.getNextAvailableId(baseId, defaultSerial);
        return ResponseEntity.ok(nextId);
    }

    /**
     * 신규 회원 등록 처리
     */
    @PostMapping("/employees/students")
    public String createStudent(MemberDto memberDto, MemberCreateLogDto memberCreateLog,
            @RequestParam(required = false) MultipartFile profileImage, Principal principal) {
        // 로그인한 관리자의 ID를 꺼냄
        String loginAdmin = "SYSTEM"; // 기본값

        if (principal != null) {
            loginAdmin = principal.getName(); // 세션이나 토큰에 저장된 로그인 ID
        }

        // 이미지는 base64로 인코딩하여 결재 페이로드에 저장 — Cloudinary 업로드는 최종 결재 후 실행
        String profileImageBase64 = null;
        String profileImageType = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                profileImageBase64 = Base64.getEncoder().encodeToString(profileImage.getBytes());
                profileImageType = profileImage.getContentType();
            } catch (Exception e) {
                log.error("[createStudent] 이미지 인코딩 실패: {}", e.getMessage());
                return "redirect:/admin/employees/students?error="
                        + URLEncoder.encode("프로필 이미지 처리에 실패했습니다.", StandardCharsets.UTF_8);
            }
        }

        try {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("memberDto", memberDto);
            data.put("memberCreateLog", memberCreateLog);
            data.put("profileImageBase64", profileImageBase64);
            data.put("profileImageType", profileImageType);

            activityApprovalService.submitForApproval(loginAdmin,
                    AdminActivityType.STUDENT_REGISTER,
                    memberDto.getUserName() + " (" + memberDto.getUserId() + ")", data);
        } catch (Exception e) {
            log.warn("[createStudent] 결재 요청 실패: {}", e.getMessage());
            return "redirect:/admin/employees/students?error="
                    + URLEncoder.encode("결재 요청에 실패했습니다: " + e.getMessage(), StandardCharsets.UTF_8);
        }

        return "redirect:/admin/employees/students?success="
                + URLEncoder.encode("결재에 등록되었습니다.", StandardCharsets.UTF_8);
    }

    /**
     * 학생 정보 수정
     */
    @PutMapping("/students/update")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateStudent(@RequestParam String userId,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String userTelno,
            @RequestParam(required = false) String userEmailAddr,
            @RequestParam(required = false) String userZip,
            @RequestParam(required = false) String userAddr,
            @RequestParam(required = false) String userDaddr,
            @RequestParam(required = false) String userProfile,
            @RequestParam(required = false) String userRole,
            @RequestParam(required = false) String enable,
            @RequestParam(required = false) MultipartFile editProfileImage, Principal principal) {
        String loginAdminId = principal != null ? principal.getName() : "SYSTEM";

        // 새 이미지는 base64로 인코딩하여 결재 페이로드에 저장 — Cloudinary 업로드는 최종 결재 후 실행
        String profileImageBase64 = null;
        String profileImageType = null;
        if (editProfileImage != null && !editProfileImage.isEmpty()) {
            try {
                profileImageBase64 =
                        Base64.getEncoder().encodeToString(editProfileImage.getBytes());
                profileImageType = editProfileImage.getContentType();
            } catch (Exception e) {
                log.error("[updateStudent] 이미지 인코딩 실패: {}", e.getMessage());
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
        memberDto.setUserProfile(profileImageBase64 != null ? "__PENDING__" : userProfile);
        memberDto.setUserRole(userRole);
        memberDto.setEnable(enable);

        try {
            // 수정 전 상태 캡처 (before)
            Map<String, Object> beforeData = new LinkedHashMap<>();
            try {
                MemberDto before = staffService.retrieveStudentById(userId);
                if (before != null)
                    beforeData.put("memberDto", before);
            } catch (Exception e) {
                log.warn("[updateStudent] before 상태 조회 실패: {}", e.getMessage());
            }

            Map<String, Object> afterData = new LinkedHashMap<>();
            afterData.put("memberDto", memberDto);
            afterData.put("profileImageBase64", profileImageBase64);
            afterData.put("profileImageType", profileImageType);

            Map<String, Object> data = new LinkedHashMap<>();
            if (!beforeData.isEmpty())
                data.put("before", beforeData);
            data.put("after", afterData);

            activityApprovalService.submitForApproval(loginAdminId,
                    AdminActivityType.STUDENT_UPDATE, userId, data);

            return ResponseEntity.ok(Map.of("result", "success", "profileUrl",
                    userProfile != null ? userProfile : "", "message", "결재에 등록되었습니다."));
        } catch (Exception e) {
            log.error("[updateStudent] 결재 요청 실패. userId={}, cause={}", userId, e.getMessage());
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "error", "message", e.getMessage()));
        }
    }

    /**
     * 학생 탈퇴 처리
     */
    @PutMapping("/students/{userId}/retirement")
    @ResponseBody
    public ResponseEntity<Map<String, String>> retireStudent(@PathVariable String userId,
            @RequestBody Map<String, String> body, Principal principal) {

        // 로그인한 관리자의 ID를 꺼냄
        String loginUserId = principal != null ? principal.getName() : "SYSTEM";

        // 요청 본문에서 탈퇴 사유 추출
        String withdrawRsn = body.get("withdrawRsn");

        try {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("userId", userId);
            data.put("withdrawRsn", withdrawRsn);

            activityApprovalService.submitForApproval(loginUserId, AdminActivityType.STUDENT_RETIRE,
                    userId, data);

            return ResponseEntity
                    .ok(Map.of("result", "success", "message", "결재 요청이 완료되었습니다. 승인 후 처리됩니다."));
        } catch (Exception e) {
            log.warn("[retireStudent] 결재 요청 실패. userId={}, cause={}", userId, e.getMessage());
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "error", "message", e.getMessage()));
        }
    }

    @PostMapping("/parent/join-message/send")
    @ResponseBody
    public ResponseEntity<Void> sendParentJoinMessage(@RequestParam String studentId,
            @RequestParam String parentPhone) {
        // String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toString();
        // TODO: Rest API 서버 URL을 환경변수나 설정파일에서 읽어오도록 수정 필요
        String baseUrl = "http://localhost:9001"; // Rest API 서버의 URL로 고정 
        parentService.sendParentJoinLink(parentPhone, baseUrl, studentId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/parent/join-message")
    public String parentJoinMessage(Model model, @RequestParam(defaultValue = "1") int page) {

        List<MemberDto> studentList = staffService.retrieveStudentList();
        model.addAttribute("studentList", studentList);

        return "admin:/staff/sendJoinMessageToParent";
    }

}
