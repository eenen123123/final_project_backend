package kr.or.ddit.finalProject.dto.employee;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 직원 급여 계좌 (EMPLOYEE_BANK_ACCOUNT)
 * 직원당 1건(1:1). 외부 결제 프로그램이 참조한다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeBankAccountDto implements Serializable {

    private String userId;        // 직원 ID (PK)
    private String bankCd;        // 은행 코드 (공통코드 702)
    private String acntNo;        // 계좌번호 (문자열)
    private String depositorNm;   // 예금주명
    private String useYn;         // 사용 여부 (Y/N)
    private String rgtrId;        // 등록자 ID
    private String lastMdfrId;    // 최종 수정자 ID
    private LocalDateTime regDt;  // 등록일시
    private LocalDateTime mdfcnDt; // 수정일시
}
