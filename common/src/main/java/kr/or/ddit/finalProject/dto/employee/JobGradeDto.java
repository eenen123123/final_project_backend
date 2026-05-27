package kr.or.ddit.finalProject.dto.employee;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobGradeDto implements Serializable {

    private String jbgrCd;
    private String jbgrNm;
    private Long sortOrd;
    private String useYn; // Y:사용 / N:미사용
    private Integer baseAnnLvDays; // 직급별 기본 부여 일수 · DEFAULT 15
    private String rgtrId;
    private String lastMdfrId;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
    private String deptCd;
}