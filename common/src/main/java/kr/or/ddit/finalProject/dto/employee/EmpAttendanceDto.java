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
public class EmpAttendanceDto implements Serializable {

    private Long atndncSn; // 기본키(PK) · 자동증가
    private String atndncUserId; // MEMBER.USER_ID 참조 (FK)
    private String atndncTypeCd; // COM_CD (CL_CODE='ATNDNC') 참조
    private LocalDateTime atndncRegDt;
    private String atndncRegIpAddr; // 근태 등록 단말 IP 출퇴근 위조 방지 로그용
}
