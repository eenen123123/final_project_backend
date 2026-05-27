package kr.or.ddit.finalProject.dto.employee;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSalaryDto implements Serializable {
    
    private Integer salaryId; // 급여 이력

    private String userId; // 직원ID
    private Integer baseSalary; // 기본 급여
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate applyYmd; // 급여 적용 시작일

    private String useYn; // 급여 적용 여부 (Y/N)
    private String rgtrId; // 등록자 ID
    private String lastMdfrId; // 최종 수정자 ID

    private LocalDateTime regDt; // 등록 일시
    private LocalDateTime mdfcnDt; // 최종 수정 일시
}
