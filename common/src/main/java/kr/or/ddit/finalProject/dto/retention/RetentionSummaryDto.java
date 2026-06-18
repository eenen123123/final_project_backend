package kr.or.ddit.finalProject.dto.retention;

import lombok.Data;

/**
 * 퇴원 방어 상단 요약 카드 DTO
 */
@Data
public class RetentionSummaryDto {
    private int totalStudents;   // 전체 재학생 (오프라인)
    private int riskCnt;         // 퇴원 위험 (고위험)
    private int attnAbnormalCnt; // 근태 이상 학생 수 (최근 30일)
    private int retainedCnt;     // 상담 결과 유지 성공
}
