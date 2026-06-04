package kr.or.ddit.controller.staff;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.service.file.CloudinaryUploadService;
import kr.or.ddit.finalProject.service.staff.StaffService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;


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
    
}
