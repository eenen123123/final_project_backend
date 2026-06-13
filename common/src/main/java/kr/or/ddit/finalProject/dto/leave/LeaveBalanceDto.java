package kr.or.ddit.finalProject.dto.leave;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 잔여 연차 현황 조회용 DTO.
 *
 * 총 연차(totalDays) = 해당 직원 직급(JOB_GRADE.BASE_ANN_LV_DAYS),
 * 사용(usedDays)     = 해당 연도 ANNUAL_LEAVE_HISTORY 의 ANN_REQ_DAYS 합계,
 * 잔여(remainDays)   = totalDays - usedDays.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalanceDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String userName;
    private String deptNm;
    private String jbgrNm;
    private Integer totalDays;       // 직급 기본 연차
    private BigDecimal usedDays;     // 해당 연도 사용 합계
    private BigDecimal remainDays;   // 잔여 (total - used)
}
