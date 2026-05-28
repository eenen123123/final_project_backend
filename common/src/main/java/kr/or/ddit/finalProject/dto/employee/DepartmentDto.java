package kr.or.ddit.finalProject.dto.employee;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDto implements Serializable {

    private String deptCd; // 예: 100행정 200미디어 300강사
    private String prntDeptCd; // NULL이면 최상위 부서
    private String deptNm;
    private String useYn; // Y:사용 / N:미사용
    private String intlTelNo; // 사무실 번호
    private String rgtrId;
    private String lastMdfrId;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
}
