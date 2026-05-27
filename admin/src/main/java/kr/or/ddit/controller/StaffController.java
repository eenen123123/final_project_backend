package kr.or.ddit.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import kr.or.ddit.finalProject.dto.employee.DepartmentDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeInfoDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeSalaryDto;
import kr.or.ddit.finalProject.dto.employee.JobGradeDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.mapper.StaffMapper;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@Controller
@RequestMapping("/admin")
public class StaffController {

    @Autowired
    PasswordEncoder passwordEncoder;
    
    @Autowired
    StaffMapper staffMapper;

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

        List<DepartmentDto> departmentlist = staffMapper.selectDepartmentList();
        List<JobGradeDto> jobgradelist = staffMapper.selectJobGradeList();

        model.addAttribute("departmentlist", departmentlist);
        model.addAttribute("jobgradelist", jobgradelist);

        return "admin:/staff/employees";
    }

    /**
     * 신규 직원 등록 처리
     * @param memberDto - 회원 정보 (아이디, 비밀번호, 이름, 연락처 등)
     * @param employeeInfoDto - 직원 정보 (부서, 직급, 입사일 등)
     * @param profileImage - 프로필 이미지 파일 (선택)
     * @return
     */
    @PostMapping("/employees")
    public String createEmployee(MemberDto memberDto,
                                 EmployeeInfoDto employeeInfoDto,
                                 EmployeeSalaryDto employeeSalary,
                                //  @RequestParam(value = "userProfile", required = false) MultipartFile profileImage) {
                                 @RequestParam(value = "tempuserProfile", required = false) MultipartFile profileImage,
                                Principal principal) {

        // 비밀번호 암호화
        memberDto.setUserEnpswd(passwordEncoder.encode(memberDto.getUserEnpswd()));
        
        // 로그인한 관리자의 ID를 꺼냄
        String loginAdminId = "SYSTEM"; // 기본값, 실제로는 principal에서 꺼내야 함
        if (principal != null) {
            loginAdminId = principal.getName(); // 세션이나 토큰에 저장된 로그인 ID
        }

        // LocalDateTime now = LocalDateTime.now();

        // MemberDto에 데이터 넣기
        // memberDto.setJoinDt(now); // 가입일자
        // memberDto.setRegDate(now); // 최초등록시점
        // memberDto.setModDate(now); // 최종등록시점

        // EmployeeInfoDto에 데이터 넣기
        employeeInfoDto.setRgtrId(loginAdminId); // 최초등록자ID -> 현재 로그인한 관리자 ID
        employeeInfoDto.setLastMdfrId(loginAdminId); // 최종등록자ID -> 현재 로그인한 관리자 ID
        // employeeInfoDto.setRegDt(now); // 최초등록시점 -> 현재 시간
        // employeeInfoDto.setMdfcnDt(now); // 최종등록시점 -> 현재 시간

        // EmployeeSalaryDto 데이터 넣기
        employeeSalary.setUserId(memberDto.getUserId());

        // 첫 등록이므로 현재 사용 여부는 무조건 'Y'로 설정
        employeeSalary.setUseYn("Y");

        // 급여 적용 시작일은 입사일과 동일하게 설정
        employeeSalary.setApplyYmd(employeeInfoDto.getJoinYmd());

        // EmployeeSalaryDto에 데이터 넣기
        employeeSalary.setRgtrId(loginAdminId); // 최초등록자ID -> 현재 로그인한 관리자 ID
        employeeSalary.setLastMdfrId(loginAdminId); // 최종등록자ID -> 현재 로그인한 관리자 ID
        // employeeSalary.setRegDt(now); // 최초등록시점 -> 현재 시간
        // employeeSalary.setMdfcnDt(now); // 최종등록시점 -> 현재 시간

        log.info("수정된 memberDto : {}", memberDto);
        log.info("수정된 employeeInfoDto : {}", employeeInfoDto);
        log.info("수정된 employeeSalaryDto : {}", employeeSalary);

        

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
