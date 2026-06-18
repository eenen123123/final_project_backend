package kr.or.ddit.finalProject.dto.consultation;

import lombok.Data;

/**
 * 상담 관리 상단 요약 카드 DTO
 */
@Data
public class ConsultationSummaryDto {
    private int monthCnt;      // 이번 달 상담
    private int scheduledCnt;  // 예정 상담 (상태 01)
    private int doneCnt;       // 완료 상담 (상태 02)
    private int followupCnt;   // 후속 조치 필요 (상태 03)
}
