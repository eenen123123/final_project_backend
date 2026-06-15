package kr.or.ddit.finalProject.dto.employee;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 급여 + 계좌 관리 화면 / CSV 내보내기용 조회 행 DTO
 * MEMBER + EMPLOYEE_INFO + EMPLOYEE_SALARY(현재) + EMPLOYEE_BANK_ACCOUNT 조인 결과.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryAccountRowDto implements Serializable {

    private String userId;       // 직원 ID
    private String userName;     // 직원명
    private String deptNm;       // 부서명
    private String jbgrNm;       // 직급명
    private String emplStatCd;   // 재직 상태 코드

    private Integer baseSalary;  // 현재 적용 기본급 (EMPLOYEE_SALARY USE_YN='Y')

    private String bankCd;       // 은행 코드 (공통코드 702)
    private String bankNm;       // 은행명 (COM_CD 조인)
    private String acntNo;       // 계좌번호
    private String depositorNm;  // 예금주명
}
