package kr.or.ddit.finalProject.dto.employee;

import kr.or.ddit.finalProject.dto.member.MemberDto;
import lombok.Data;

/**
 * 회원(MEMBER), 직원 상세(EMPLOYEE_INFO), 부서 및 직급 명칭을 
 * 하나의 묶음으로 처리하여 화면에 내려주기 위한 통합 인사 상세 DTO 클래스이다.
 */
@Data
public class EmployeeDetailDto {

    // MyBatis 최상위 <id> 매핑용 (M_USER_ID → 중복 병합 방지)
    private String userId;

    // 1. 회원 마스터 테이블(MEMBER)의 인증 계정 및 개인정보 객체를 맵핑한다.
    private MemberDto member;

    // 2. 직원 인사 관리 테이블(EMPLOYEE_INFO)의 발령 및 계약 기간 정보 객체를 맵핑한다.
    private EmployeeInfoDto employeeInfo;

    // 3. DEPARTMENT 테이블과의 조인을 통해 도출된 실제 부서명(Text)을 저장해야 한다.
    private String deptNm;

    // 4. JOB_GRADE 테이블과의 조인을 통해 도출된 실제 직급명(Text)을 저장해야 한다.
    private String jbgrNm;

    // 5. 직원의 현재 부서
    private String deptCd;

    // 6. 직원 상태(재직, 휴직, 퇴사) 
    private String emplStatCd;

    // 7. 직원의 근무 형태(정규직, 계약직, 파트타임)
    private String emplTypeCd;
}