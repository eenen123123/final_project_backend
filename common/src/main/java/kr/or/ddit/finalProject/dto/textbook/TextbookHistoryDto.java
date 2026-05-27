package kr.or.ddit.finalProject.dto.textbook;

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
public class TextbookHistoryDto implements Serializable {

    private Long invtHistSn; // 복합PK(1/2) 시퀀스
    private Long textbookSn; // 복합PK(2/2)
    private String ioTypeCd; // COM_CD 공통코드 참조
    private Integer chgCnt; // 입고(+) / 출고(-) 구분 저장 권장
    private Integer bfrChgCnt;
    private Integer aftChgCnt;
    private String relDutyTypeCd; // COM_CD 공통코드 참조
    private Long relDutyId;
    private LocalDateTime procDt;
    private String procUserId;
}
