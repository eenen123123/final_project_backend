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

    private Long invtSn; // 재고 일련번호
    private Long textbookSn; // 교재 일련번호
    private Integer totInvtCnt; // 총 재고 수량
    private Integer salableCnt; // 판매 가능 수량
    private Integer saleCmplCnt; // 판매 완료 수량
    private Integer rsrvWaitCnt; // 예약 대기 수량
    private Integer dmgdDspslCnt; // 파손/폐기 수량
    private Integer minKeepCnt; // 최소 유지 수량 (이하면 재입고 필요)
    private InventoryStatus invtStatCd; // 재고 상태
    private String rgtrId;
    private String lastMdfrId;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
}
