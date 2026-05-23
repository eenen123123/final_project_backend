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
public class StudentBlackListHistoryDto implements Serializable {

    private Long blklstHistSn;
    private String blklstStdUserId;
    private LocalDateTime blklstRegDt;
    private String blklstRsnCn;
    private Integer blklstImpsDaysCnt; // 0 또는 NULL = 영구 제한
    private String blklstRgtrUserId; // 시스템 자동 등록 시 NULL 허용
}