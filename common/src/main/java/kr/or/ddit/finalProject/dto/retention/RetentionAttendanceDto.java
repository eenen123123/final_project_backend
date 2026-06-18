package kr.or.ddit.finalProject.dto.retention;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 근태 특이사항 집계 DTO (퇴원 방어 - 근태 탭)
 * STUDENT_ATTENDANCE 를 학생별로 집계 (최근 30일, 오프라인 학생)
 * 위험도 = 결석×3 + 조퇴×2 + 지각×1 → 고(≥12)/중(≥6)/저
 */
@Data
public class RetentionAttendanceDto {

    private String stdUserId;        // 학생 ID
    private String studentNm;        // 학생명
    private String className;        // 담당 반/강좌명
    private int absentCnt;           // 결석 횟수 (코드 02)
    private int lateCnt;             // 지각 횟수 (코드 03)
    private int earlyCnt;            // 조퇴 횟수 (코드 04)
    private String lastNote;         // 최근 특이사항 메모
    private LocalDateTime lastNoteDt;// 최근 특이사항 일시
    private int riskScore;           // 위험 점수
    private String riskLevel;        // 위험도 (고위험/중위험/저위험)
}
