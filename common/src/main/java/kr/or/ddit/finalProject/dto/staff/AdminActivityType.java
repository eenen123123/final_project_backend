package kr.or.ddit.finalProject.dto.staff;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdminActivityType {
    EMPLOYEE_REGISTER("직원 신규 등록"),
    EMPLOYEE_UPDATE("직원 정보 수정"),
    EMPLOYEE_RETIRE("직원 퇴사 처리"),
    STUDENT_REGISTER("학생 신규 등록"),
    STUDENT_UPDATE("학생 정보 수정"),
    STUDENT_RETIRE("학생 탈퇴 처리"),
    DEPT_CREATE("부서 등록"),
    DEPT_UPDATE("부서 수정"),
    DEPT_TOGGLE("부서 활성화 변경"),
    GRADE_CREATE("직급 등록"),
    GRADE_UPDATE("직급 수정"),
    GRADE_TOGGLE("직급 활성화 변경"),
    MNT_MAPPING("사수 배정");

    private final String label;
}
