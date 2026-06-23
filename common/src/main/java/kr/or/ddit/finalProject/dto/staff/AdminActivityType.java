package kr.or.ddit.finalProject.dto.staff;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdminActivityType {
    EMPLOYEE_REGISTER("직원 신규 등록",  "EMP_MGMT"),
    EMPLOYEE_UPDATE  ("직원 정보 수정",  "EMP_MGMT"),
    EMPLOYEE_RETIRE  ("직원 퇴사 처리",  "EMP_MGMT"),
    SALARY_ACCOUNT_UPDATE("급여·계좌 변경", "EMP_MGMT"),
    STUDENT_REGISTER ("학생 신규 등록",  "STU_MGMT"),
    STUDENT_UPDATE   ("학생 정보 수정",  "STU_MGMT"),
    STUDENT_RETIRE   ("학생 탈퇴 처리",  "STU_MGMT"),
    DEPT_CREATE      ("부서 등록",       "ORG_MGMT"),
    DEPT_UPDATE      ("부서 수정",       "ORG_MGMT"),
    DEPT_TOGGLE      ("부서 활성화 변경","ORG_MGMT"),
    GRADE_CREATE     ("직급 등록",       "ORG_MGMT"),
    GRADE_UPDATE     ("직급 수정",       "ORG_MGMT"),
    GRADE_TOGGLE     ("직급 활성화 변경","ORG_MGMT"),
    MNT_MAPPING      ("사수 배정",       "ORG_MGMT"),
    COMMON_CODE_GROUP_CREATE("공통코드 분류 등록", "COM_CODE"),
    COMMON_CODE_GROUP_UPDATE("공통코드 분류 수정", "COM_CODE"),
    COMMON_CODE_GROUP_DELETE("공통코드 분류 삭제", "COM_CODE"),
    COMMON_CODE_CREATE      ("공통코드 등록",      "COM_CODE"),
    COMMON_CODE_UPDATE      ("공통코드 수정",      "COM_CODE"),
    COMMON_CODE_DELETE      ("공통코드 삭제",      "COM_CODE"),
    LEAVE_REQUEST           ("휴가 신청",         "LEAVE_MGMT"),
    
    // 과목 관리
    SUBJECT_CL_CREATE("대분류 등록", "SUBJECT_MGMT"),
    SUBJECT_CL_UPDATE("대분류 수정", "SUBJECT_MGMT"),
    SUBJECT_CL_DELETE("대분류 삭제", "SUBJECT_MGMT"),
    SUBJECT_CREATE("과목 등록",      "SUBJECT_MGMT"),
    SUBJECT_UPDATE("과목 수정",      "SUBJECT_MGMT"),
    SUBJECT_DELETE("과목 삭제",      "SUBJECT_MGMT"),

    // 커리큘럼 관리
    CURRICULUM_CREATE("커리큘럼 등록", "CURRICULUM_MGMT"),
    CURRICULUM_UPDATE("커리큘럼 수정", "CURRICULUM_MGMT"),
    CURRICULUM_DELETE("커리큘럼 삭제", "CURRICULUM_MGMT"),

    // 클래스룸 관리
    CLASSROOM_CREATE("클래스룸 등록", "CURRICULUM_MGMT");

    private final String label;
    private final String tmplCd;
}
