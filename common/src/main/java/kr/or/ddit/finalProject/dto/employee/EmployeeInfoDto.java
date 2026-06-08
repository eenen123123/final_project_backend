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
public class EmployeeInfoDto implements Serializable {

    private String userId; // 기본키(PK)
    private String deptCd; // 부서 테이블 참조(FK)
    private String jbgrCd; // 직급 테이블 참조(FK)

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate joinYmd; // 실제 출근 시작 날짜 (시/분/초 제외)

    private LocalDateTime retmtYmd;
    private String retmtRsn; // 퇴사 사유 (최대 1000자)
    private String emplStatCd; // 재직,휴직,퇴사 등 상태값 (01:재직 02:휴직 03:퇴사)
    private String atchFileId; // 공통첨부파일분류
    private String chrgDutyCn;
    private String rgtrId;
    private String lastMdfrId;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;

    private String emplTypeCd; // 직원 유형 (01:정규직, 02:계약직, 03:파트타임)
    private String ctrctEndYmd; // 계약 종료 날짜 (정규직은 null 허용)

    private String mntUserId; // 사수 ID

    private String deptNm; // 부서명
    private String jbgrNm; // 직급명
    private Integer sortOrd; // 직급 정렬순서 (낮을수록 상위직급)
    private String userName; // 사용자명 (MEMBER.USER_NAME)
}
