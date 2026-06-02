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
public class TextbookInventoryDto implements Serializable {

    private Long invtSn; // 복합PK(1/2) 시퀀스
    private Long textbookSn; // 복합PK(2/2)
    private Integer totInvtCnt;
    private Integer salableCnt;
    private Integer saleCmplCnt;
    private Integer rsrvWaitCnt;
    private Integer dmgdDspslCnt;
    private Integer minKeepCnt;
    private String invtStatCd; // COM_CD 공통코드 참조
    private String rgtrId;
    private String lastMdfrId;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
}
