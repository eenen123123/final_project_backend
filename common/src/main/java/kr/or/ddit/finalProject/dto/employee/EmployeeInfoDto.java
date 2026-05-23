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
public class EmployeeInfoDto implements Serializable {

    private String userId; // 기본키(PK)
    private String deptCd; // 부서 테이블 참조(FK)
    private String jbgrCd; // 직급 테이블 참조(FK)
    private LocalDateTime joinYmd; // 실제 출근 시작 날짜
    private LocalDateTime retmtYmd;
    private String emplStatCd; // 재직,휴직,퇴사 등 상태값 (01:재직 02:휴직 03:퇴사)
    private String atchFileId; // 공통첨부파일분류
    private String chrgDutyCn;
    private String rgtrId;
    private String lastMdfrId;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
}