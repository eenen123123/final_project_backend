package kr.or.ddit.controller.staff;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.or.ddit.finalProject.dto.employee.DepartmentDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeDetailDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeInfoDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeSalaryDto;
import kr.or.ddit.finalProject.dto.employee.JobGradeDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.service.file.CloudinaryUploadService;
import kr.or.ddit.finalProject.service.staff.StaffService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.HashMap;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;


@Slf4j
@Controller
@RequestMapping("/admin")
public class StaffEmployeesController {

    @Autowired
    StaffService staffService;

    @Autowired
    CloudinaryUploadService cloudinaryUploadService;

    /**
     * 직원 관리 메인 화면 이동 및 초기 데이터 조회
     * 
     * ✔ 직원 정보 및 계정 관리 페이지(인사 관리 대시보드)를
     * 요청할 때 진입하는 컨트롤러 메서드
     * 
     * ✔ 역할 요약
     * ---------------------------------------------------------------------
     * - 전체 직원 목록 데이터 확보
     * - 검색 및 필터링용 메타데이터(부서, 직급, 입사 연도) 조회
     * - 뷰(View) 템플릿으로 데이터 전달 및 포워딩
     * 
     * ✔ 설계 목적
     * ---------------------------------------------------------------------
     * 1. 인사 관리 화면 초기 로딩 시 필요한 모든 데이터를 한 번에 바인딩
     * 2. 화면 단의 검색 조건 필터(Select Box) 구성 요소들을 동적으로 제공
     * 
     * ✔ 아키텍처 위치 (Controller Layer)
     * ---------------------------------------------------------------------
     * [Bower] -> [staffController.getEmployees()] -> [staffService]
     * ↓
     * [admin:/staff/employees] <- (Model Data Binding) <- [Database]
     * 
     * @param model 화면(Thymeleaf)에 조화된 데이터를 전달하기 위한 Spring UI Model 객체
     * @return 직원 관리 메인 뷰 템플릿(HTML) 경로
     */
    @GetMapping("/employees")
    public String getEmployees(Model model) {
        log.info("getEmployees()");

        // 1. 인사 관리 대시보드 테이블에 노출할 전체 직원 상세 목록 데이터를 조회한다.
        List<EmployeeDetailDto> employeeList = staffService.retrieveEmployeeList();

        // 2. 화면 상단 검색 조건 필터(Select Box) 구성을 위한 부서명 메타데이터 목록을 조회한다.
        List<DepartmentDto> departmentlist = staffService.retrieveDepartmentList();

        //3. 화면 상단 검색 조건 필터 구성을 위한 직급명 메타데이터 목록을 조회한다.
        List<JobGradeDto> jobgradelist = staffService.retrieveJobGradeList();

        //4. 입사 연도별 필터링 기능을 지원하기 위해 시스템에 등록된 전체 입사 연도 목록을 조회한다.
        List<Integer> joinYearList = staffService.retrieveJoinYearList();

        // 5. 조회된 4가지 목록 데이터를 Thymeleaf 템플릿 엔진에서 식별할 수 있도록 Model 객체에 바인딩한다.
        model.addAttribute("departmentlist", departmentlist);
        model.addAttribute("jobgradelist", jobgradelist);
        model.addAttribute("joinYearList", joinYearList);
        model.addAttribute("employeeList", employeeList);

        // 6. 직원 목록 화면을 렌더링할 admin 권한 전용 staff/employees 뷰 템플릿 경로를 반환한다.
        return "admin:/staff/employees";
    }

    /**
     * 신규 직원 등록 처리
     * @param memberDto // 회원 정보 DTO
     * @param employeeInfoDto // 직원 정보 DTO
     * @param employeeSalaryDto // 직원 급여 DTO
     * @param profileImage // 프로필 이미지 파일
     * @param principal // 현재 로그인한 관리자 정보
     * @return
     */
    @PostMapping("/employees")
    public String createEmployee(
        MemberDto memberDto,
        EmployeeInfoDto employeeInfoDto,
        EmployeeSalaryDto employeeSalary,
        @RequestParam(required = false) MultipartFile profileImage,
        Principal principal
    ) {

        // 로그인한 관리자의 ID를 꺼냄
        String loginAdminId = "SYSTEM"; // 기본값, 실제로는 principal에서 꺼내야 함
        if (principal != null) {
            loginAdminId = principal.getName(); // 세션이나 토큰에 저장된 로그인 ID
        }

        // 프로필 사진 cloudinary 업로드
        String profileUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                profileUrl = cloudinaryUploadService.uploadFileToCloudinary(profileImage);
            } catch (Exception e) {
                log.error("[createEmployee] Cloudinary 업로드 실패: {}", e.getMessage());
                return "redirect:/admin/employees?error=" + URLEncoder.encode("프로필 이미지 업로드에 실패했습니다.", StandardCharsets.UTF_8);
            }
        }

        try {
            staffService.registerEmployee(memberDto, employeeInfoDto, employeeSalary, profileUrl, loginAdminId);
        } catch (FinalProjectException e) {
            log.warn("[createEmployee] 등록 실패: {}", e.getMessage());
            return "redirect:/admin/employees?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            log.warn("[createEmployee] 유효성 검사 실패: {}", e.getMessage());
            return "redirect:/admin/employees?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }

        return "redirect:/admin/employees?success=" + URLEncoder.encode("직원이 성공적으로 등록되었습니다.", StandardCharsets.UTF_8);
    }

    private static final int HR_SCREEN_SIZE = 10;
    private static final int HR_BLOCK_SIZE  = 5;

    /**
     * 직원 목록 동적 검색 + 서버 페이징 (AJAX)
     */
    @GetMapping("/employees/search")
    @ResponseBody
    public ResponseEntity<PageResponse<EmployeeDetailDto>> searchEmployees(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String deptCd,
            @RequestParam(required = false) String jbgrCd,
            @RequestParam(required = false) String emplTypeCd,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int screenSize,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String orderDirection) {

        Map<String, Object> searchParams = new HashMap<>();
        if (keyword != null && !keyword.isBlank())       searchParams.put("keyword",    keyword.trim());
        if (year != null && !year.isBlank())             searchParams.put("year",       year.trim());
        if (status != null && !status.isBlank())         searchParams.put("status",     status.trim());
        if (deptCd != null && !deptCd.isBlank())         searchParams.put("deptCd",     deptCd.trim());
        if (jbgrCd != null && !jbgrCd.isBlank())         searchParams.put("jbgrCd",     jbgrCd.trim());
        if (emplTypeCd != null && !emplTypeCd.isBlank()) searchParams.put("emplTypeCd", emplTypeCd.trim());

        String safeDir = "DESC".equalsIgnoreCase(orderDirection) ? "DESC" : "ASC";
        PaginationInfo<Map<String, Object>> paging =
            new PaginationInfo<>(screenSize, HR_BLOCK_SIZE, page, orderBy, safeDir);
        paging.setDetailCondition(searchParams);

        return ResponseEntity.ok(staffService.searchEmployeeList(paging));
    }

    /**
     * 아이디 중복 자동 순번 발급 및 중복 회피
     */
    @GetMapping("/employees/next-id")
    @ResponseBody
    public ResponseEntity<String> getNextId(@RequestParam String baseId, @RequestParam String defaultSerial) {
        // 이 메서드 하나가 check-id의 역할까지 포함하여 다음 사용 가능 ID를 보장합니다.
        String nextId = staffService.getNextAvailableId(baseId, defaultSerial);
        return ResponseEntity.ok(nextId); 
    }

    /**
     * 직원 정보 수정
     * @param principal // 현재 로그인한 관리자 정보
     * @return
     */
    @PutMapping("/employees/update")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateEmployee(
            @RequestParam String userId,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String userGndrCd,
            @RequestParam(required = false) String userTelno,
            @RequestParam(required = false) String userEmailAddr,
            @RequestParam(required = false) String userZip,
            @RequestParam(required = false) String userAddr,
            @RequestParam(required = false) String userDaddr,
            @RequestParam(required = false) String userBrdt,
            @RequestParam(required = false) String userProfile,
            @RequestParam(required = false) String deptCd,
            @RequestParam(required = false) String jbgrCd,
            @RequestParam(required = false) String joinYmd,
            @RequestParam(required = false) String emplStatCd,
            @RequestParam(required = false) String emplTypeCd,
            @RequestParam(required = false) String ctrctEndYmd,
            @RequestParam(required = false) String chrgDutyCn,
            @RequestParam(required = false) Integer baseSalary,
            @RequestParam(required = false) MultipartFile editProfileImage,
            Principal principal
    ) {

        // 로그인한 관리자의 ID를 꺼냄
        String loginAdminId = principal != null ? principal.getName() : "SYSTEM";

        // 새 파일이 있으면 Cloudinary 업로드, 없으면 기존 URL 유지
        String finalProfileUrl = userProfile;
        if (editProfileImage != null && !editProfileImage.isEmpty()) {
            try {
                finalProfileUrl = cloudinaryUploadService.uploadFileToCloudinary(editProfileImage);
            } catch (Exception e) {
                log.error("[updateEmployee] Cloudinary 업로드 실패 : {}", e.getMessage());
            }
        }

        MemberDto memberDto = new MemberDto();
        memberDto.setUserId(userId);
        memberDto.setUserName(userName);
        memberDto.setUserGndrCd(userGndrCd);
        memberDto.setUserTelno(userTelno);
        memberDto.setUserEmailAddr(userEmailAddr);
        memberDto.setUserZip(userZip);
        memberDto.setUserAddr(userAddr);
        memberDto.setUserDaddr(userDaddr);
        memberDto.setUserProfile(finalProfileUrl);
        if (userBrdt != null && !userBrdt.isBlank()) {
            memberDto.setUserBrdt(java.time.LocalDate.parse(userBrdt.substring(0, 10)));
        }

        // 직원 정보 DTO 생성 및 설정
        EmployeeInfoDto employeeInfoDto = new EmployeeInfoDto();
        employeeInfoDto.setUserId(userId);
        employeeInfoDto.setDeptCd(deptCd);
        employeeInfoDto.setJbgrCd(jbgrCd);
        employeeInfoDto.setEmplStatCd(emplStatCd);
        employeeInfoDto.setEmplTypeCd(emplTypeCd);
        employeeInfoDto.setChrgDutyCn(chrgDutyCn);
        if (joinYmd != null && !joinYmd.isBlank()) {
            employeeInfoDto.setJoinYmd(java.time.LocalDate.parse(joinYmd.substring(0, 10)));
        }
        if (ctrctEndYmd != null && !ctrctEndYmd.isBlank()) {
            employeeInfoDto.setCtrctEndYmd(ctrctEndYmd.substring(0, 10));
        }

        // 직원 급여 DTO 생성 및 설정
        EmployeeSalaryDto employeeSalaryDto = new EmployeeSalaryDto();
        if (baseSalary != null) {
            employeeSalaryDto.setBaseSalary(baseSalary);
        }

        // 서비스 호출하여 직원 정보 업데이트
        try {
            staffService.updateEmployee(memberDto, employeeInfoDto, employeeSalaryDto, loginAdminId);
            return ResponseEntity.ok(Map.of("result", "success", "profileUrl", finalProfileUrl != null ? finalProfileUrl : ""));
        } catch (Exception e) {
            log.error("[updateEmployee] 수정 실패. userId={}, cause={}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "error", "message", e.getMessage()));
        }
    }
    
    /**
     * 직원 퇴사 처리
     * @param userId
     * @param body
     * @param principal
     * @return
     */
    @PutMapping("/employees/{userId}/retirement")
    @ResponseBody
    public ResponseEntity<Map<String, String>>retireEmployee(
        @PathVariable String userId, 
        @RequestBody Map<String, String> body,
        Principal principal
    ) {
        
        // 로그인한 관리자의 ID를 꺼냄
        String loginUserId = principal != null ? principal.getName() : "SYSTEM";

        // 요청 본문에서 퇴사 사유 추출
        String retmtRsn = body.get("retmtRsn");

        // 서비스 호출하여 직원 퇴사 처리
        try {
            staffService.retireEmployee(userId, retmtRsn, loginUserId);
            return ResponseEntity.ok(Map.of("result", "success"));
        } catch (FinalProjectException e) {
            log.warn("[retireEmployee] 퇴사 처리 실패. userId={}, cause={}", userId, e.getMessage());
            return ResponseEntity.status(e.getErrorCode().getStatus())
                                .body(Map.of("result", "error", "message", e.getMessage()));
        }

    }



}
