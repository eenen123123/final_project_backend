package kr.or.ddit.finalProject.dto.student;

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
public class StudentAttendanceDto implements Serializable {

    private Long atndSn; // PK · 시퀀스
    private String stdUserId;
    private String atndTypeCd; // COM_CD 공통코드 참조
    private LocalDateTime atndRegDt;
    private String atndRegIpAddr; // (IPv4 최대 15자)
    private String atndNoteCn; // 출결 관련 메모·사유 등
}