package kr.or.ddit.finalProject.dto.exam;

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
public class ExamDto implements Serializable {

    private Long examSn; // 기본키(PK) · 시퀀스
    private String examTypeCd; // COM_CD 공통코드 참조
    private String examChrgUserId;
    private String examRegNm;
    private LocalDateTime examRegDt;
    private LocalDateTime examStrtDt;
    private LocalDateTime examEndDt;
    private String examStatCd; // COM_CD 참조
}