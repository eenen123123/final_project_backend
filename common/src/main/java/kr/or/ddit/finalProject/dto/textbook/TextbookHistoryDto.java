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

    private Long invtHistSn; // 입출고 일련번호
    private Long textbookSn; // 교재 일련번호
    private String ioTypeCd; // 입출고 유형 (10:입고 / 20:출고)
    private Integer chgCnt; // 변경 수량 · 입고(+) / 출고(-) 구분 저장 권장
    private Integer bfrChgCnt; // 변경 전 수량
    private Integer aftChgCnt; // 변경 후 수량
    private RelDutyType relDutyTypeCd; // 관련 업무 유형 (null 허용)
    private Long relDutyId; // 관련 업무 ID (null 허용)
    private LocalDateTime procDt; // 처리일시
    private String procUserId; // 처리자 ID

    // 검색 조건 (DB 컬럼 없음)
    private String keyword; // 검색 키워드 (처리자 ID 등)
    private String startDt; // 검색 시작일 (YYYY-MM-DD)
    private String endDt; // 검색 종료일 (YYYY-MM-DD)
}
