package kr.or.ddit.finalProject.dto.student;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 주의 학생(블랙리스트) 현재 상태 DTO.
 *
 * STUDENT_BLACK_LIST 한 행 = 학생 1명의 현재 상태.
 * 목록/상세 화면에서는 STUDENT·MEMBER·CLASSROOM·COM_CD·HISTORY 를 조인한
 * 표시용 필드까지 함께 담는다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentBlackListDto implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── STUDENT_BLACK_LIST 컬럼 ──
    private String stdUserId;
    private LocalDateTime blklstStrtDt;
    private LocalDateTime blklstEndDt;   // NULL = 적용 중, NOT NULL = 해제
    private String blklstLvlCd;          // 위험 등급 (공통코드 cl 700: 01 고위험 / 02 관찰)
    private String blklstCtgrCd;         // 유형     (공통코드 cl 701)
    private String blklstActnCn;         // 조치 내용
    private String blklstRsnCn;          // 현재 사유 (denormalize)
    private Integer blklstImpsDaysCnt;   // 정지 일수 (0/NULL = 영구) — END_DT 계산용 입력값
    private Boolean forcePermanent;      // 입력 전용: true면 누적 무시하고 즉시 영구정지

    // ── JOIN 표시용 ──
    private String userName;     // 학생명 (MEMBER)
    private String classNm;      // 수강중 클래스룸명 (오프라인 학생만, 다중이면 콤마 결합)
    private String blklstLvlNm;  // 등급명 (COM_CD)
    private String blklstCtgrNm; // 유형명 (COM_CD)
    private String status;       // 파생: 'active' | 'resolved'

    // ── 이력 파생 ──
    private String regUserId;    // 최초 등록자 ID (HISTORY REG)
    private String regUserName;  // 최초 등록자명
    private LocalDateTime regDt; // 최초 등록 일시
    private int logCount;        // 이력 건수
}
