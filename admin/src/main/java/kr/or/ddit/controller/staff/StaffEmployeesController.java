package kr.or.ddit.controller.staff;

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
import kr.or.ddit.finalProject.service.staff.StaffService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@Slf4j
@Controller
@RequestMapping("/admin")
public class StaffEmployeesController {

    @Autowired
    StaffService staffService;

    /**
     * 직원 정보 및 계정 관리
     * @param model
     * @return
     */
    @GetMapping("/employees")
    public String getEmployees(Model model) {
        log.info("getEmployees()");

        // 전체 직원 조회
        List<EmployeeDetailDto> employeeList = staffService.retrieveEmployeeList();

        // 부서명 조회
        List<DepartmentDto> departmentlist = staffService.retrieveDepartmentList();

        // 직급명 조회
        List<JobGradeDto> jobgradelist = staffService.retrieveJobGradeList();

        // 입사 연도 목록 조회
        List<Integer> joinYearList = staffService.retrieveJoinYearList();

        model.addAttribute("departmentlist", departmentlist);
        model.addAttribute("jobgradelist", jobgradelist);
        model.addAttribute("joinYearList", joinYearList);
        model.addAttribute("employeeList", employeeList);

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

        // 로그인한 관리자의 ID를 꺼냄
        String loginAdminId = "SYSTEM"; // 기본값, 실제로는 principal에서 꺼내야 함
        if (principal != null) {
            loginAdminId = principal.getName(); // 세션이나 토큰에 저장된 로그인 ID
        }

        staffService.registerEmployee(memberDto, employeeInfoDto, employeeSalary, profileImage, loginAdminId);

        return "redirect:/admin/employees";
    }

    // 아이디 중복 자동 순번 발급 및 중복 회피
    @GetMapping("/employees/next-id")
    @ResponseBody
    public ResponseEntity<String> getNextId(@RequestParam String baseId, @RequestParam String defaultSerial) {
        // 이 메서드 하나가 check-id의 역할까지 포함하여 다음 사용 가능 ID를 보장합니다.
        String nextId = staffService.getNextAvailableId(baseId, defaultSerial);
        return ResponseEntity.ok(nextId); 
    }

    // 직원 정보 수정
    @PostMapping("/employees/update")
    public String postMethodName(@RequestBody String entity) {
        
        return entity;
    }
    
    
    

}
