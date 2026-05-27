package kr.or.ddit.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import kr.or.ddit.finalProject.dto.employee.DepartmentDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeInfoDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeSalaryDto;
import kr.or.ddit.finalProject.dto.employee.JobGradeDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.mapper.StaffMapper;
import kr.or.ddit.finalProject.service.staff.StaffService;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@Controller
@RequestMapping("/admin")
public class StaffController {

    @Autowired
    PasswordEncoder passwordEncoder;
    
    @Autowired
    StaffService staffService;

    /**
     * 원비 및 수납 관리
     * @return
     */
    @GetMapping("/billing")
    public String getBilling() {
        log.info("getBilling()");
        return "admin:/staff/billing";
    }

    /**
     * 지출 및 영수증 관리
     * @return
     */
    @GetMapping("/expenses")
    public String getExpenses() {
        log.info("getExpenses()");
        return "admin:/staff/expenses";
    }

    /**
     * 출결 관리
     */
    @GetMapping("/attendance")
    public String getAttendance() {
        log.info("getAttendance()");
        return "admin:/staff/attendance";
    }

    /**
     * 교재 및 물류 관리
     */
    @GetMapping("/logistics")
    public String getLogistics() {
        log.info("getLogistics()");
        return "admin:/staff/logistics";
    }

    /**
     * 직원 정보 및 계정 관리
     */
    @GetMapping("/employees")
    public String getEmployees(Model model) {
        log.info("getEmployees()");

        List<DepartmentDto> departmentlist = staffService.retrieveDepartmentList();
        List<JobGradeDto> jobgradelist = staffService.retrieveJobGradeList();

        model.addAttribute("departmentlist", departmentlist);
        model.addAttribute("jobgradelist", jobgradelist);

        return "admin:/staff/employees";
    }

    /**
     * 신규 직원 등록 처리
     * @param memberDto // 회원 정보 DTO
     * @param employeeInfoDto // 직원 정보 DTO
     * @param employeeSalary // 직원 급여 정보 DTO
     * @param profileImage // 프로필 이미지 파일
     * @param principal // 현재 로그인한 관리자 정보 (세션에서 꺼내야 함)
     * @return
     */
    @PostMapping("/employees")
    public String createEmployee(
        // @Valid MemberDto memberDto, // @Valid 를 붙여 유효성 검증을 활성화한다.
        // BindingResult bindingResult, // 검증 실패 결과 리포트를 담을 그릇을 바로 뒤에 선언해야 한다.
        MemberDto memberDto,
        EmployeeInfoDto employeeInfoDto,
        EmployeeSalaryDto employeeSalary,
        //  @RequestParam(value = "userProfile", required = false) MultipartFile profileImage) {
        @RequestParam(value = "tempuserProfile", required = false) MultipartFile profileImage,
        Principal principal
    ) {

        // 추후 MemberDto 유효성 추가될시 아래와 같이 유효성 체크 후 예외 처리 필요
        // if (bindingResult.hasErrors()) {
        //     // 콘솔이나 로그에 어떤 에러가 터졌는지 친절하게 기록을 남긴다.
        //     log.warn("직원 등록 유효성 검증 실패: {}", bindingResult.getAllErrors());
            
        //     // 타임리프 화면으로 DTO 객체들을 다시 보내주어 기존 입력값을 유지시킨다.
        //     model.addAttribute("memberDto", memberDto);
        //     model.addAttribute("employeeInfoDto", employeeInfoDto);
        //     model.addAttribute("employeeSalaryDto", employeeSalary);
            
        //     // 알림을 보여줄 직원 등록 화면(Form)의 Thymeleaf 뷰 경로를 문자열로 리턴한다.
        //     return "admin/employeeForm"; 
        // }


        // ROLE 설정 (예: ROLE_ADMIN)
        memberDto.setUserRole("ROLE_ADMIN"); // 기본적으로 ROLE_ADMIN으로 설정

        // 비밀번호 암호화
        memberDto.setUserEnpswd(passwordEncoder.encode(memberDto.getUserEnpswd()));
        
        // 로그인한 관리자의 ID를 꺼냄
        String loginAdminId = "SYSTEM"; // 기본값, 실제로는 principal에서 꺼내야 함
        if (principal != null) {
            loginAdminId = principal.getName(); // 세션이나 토큰에 저장된 로그인 ID
        }

        // EmployeeInfoDto에 데이터 넣기
        employeeInfoDto.setRgtrId(loginAdminId); // 최초등록자ID -> 현재 로그인한 관리자 ID
        employeeInfoDto.setLastMdfrId(loginAdminId); // 최종등록자ID -> 현재 로그인한 관리자 ID

        // EmployeeSalaryDto 데이터 넣기
        employeeSalary.setUserId(memberDto.getUserId());
        employeeSalary.setUseYn("Y"); // 첫 등록이므로 현재 사용 여부는 무조건 'Y'로 설정
        employeeSalary.setApplyYmd(employeeInfoDto.getJoinYmd()); // 급여 적용 시작일은 입사일과 동일하게 설정
        employeeSalary.setRgtrId(loginAdminId); // 최초등록자ID -> 현재 로그인한 관리자 ID
        employeeSalary.setLastMdfrId(loginAdminId); // 최종등록자ID -> 현재 로그인한 관리자 ID

        // 프로필 이미지 처리 테스트 (실제 파일 저장 로직은 구현 필요)
        memberDto.setUserProfile("/images/default-profile.png"); // 기본 프로필 이미지 경로 설정

        // TELNO 하이픈 제거
        if (memberDto.getUserTelno() != null) {
            String cleanTelno = memberDto.getUserTelno().replaceAll("-","");
            memberDto.setUserTelno(cleanTelno);
        }

        // 일괄 트랜잭션 등록 처리
        staffService.registerEmployee(memberDto); // 직원 등록
        staffService.saveEmployeeInfo(employeeInfoDto); // 직원 정보 저장
        staffService.saveEmployeeSalary(employeeSalary); // 직원 급여 정보 저장

        return "redirect:/admin/employees";
    }
    

    /**
     * 근태 및 휴가 관리
     */
    @GetMapping("/hr/leave")
    public String getHrLeave() {
        log.info("getHrLeave()");
        return "admin:/staff/hr_leave";
    }

    /** 증명서 발급 관리 */
    @GetMapping("/certificates")
    public String getCertificates() {
        log.info("getCertificates()");
        return "admin:/staff/certificates";
    }

    /**
     * 시설 및 업체 관리
     */
    @GetMapping("/facilities")
    public String getFacilities() {
        log.info("getFacilities()");
        return "admin:/staff/facilities";
    }

    /**
     * 알림 발송
     */
    @GetMapping("/notifications/parent")
    public String getNotificationsParent() {
        log.info("getNotificationsParent()");
        return "admin:/staff/notifications_parent";
    }

    /**
     * 블랙리스트 관리
     */
    @GetMapping("/blacklist")
    public String getBlacklist() {
        log.info("getBlacklist()");
        return "admin:/staff/blacklist";
    }
    
    
    
    
    
    
    
    
    
    
}
