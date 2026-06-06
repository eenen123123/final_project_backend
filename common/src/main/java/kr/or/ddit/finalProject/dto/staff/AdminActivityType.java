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
    STUDENT_RETIRE("학생 탈퇴 처리");

    private final String label;
}
