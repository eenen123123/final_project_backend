package kr.or.ddit.finalProject.dto.exam;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamTakerDto implements Serializable {

    private String stdUserId;
    private Long examSn;
    private String stdNm;
    private BigDecimal totScore;  // 총점 (null=미채점)
    private String sbmtDt;        // 제출일시 (yyyy-MM-dd HH:mm)
    private String gradedYn;      // 채점완료 여부 (Y: 전 문항 채점 완료, N: 미채점 문항 존재)
}
