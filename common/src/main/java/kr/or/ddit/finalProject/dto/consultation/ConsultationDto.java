package kr.or.ddit.finalProject.dto.consultation;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 상담 기록 DTO (전체 상담 이력 / 학생별 조회 / 상세 보기 공통)
 * CONSULTATION + MEMBER(학생/학부모/담당강사) + COM_CD(유형/상태) 조인 결과
 */
@Data
public class ConsultationDto {

    private Long cnslSn;            // 상담 일련번호 (PK)
    private String stdUserId;      // 재원생 ID (신규 문의자는 null)
    private String studentNm;      // 대상자명 (재원생: MEMBER, 신규 문의자: CNSL_NM)
    private String cnslNm;         // 신규 문의자 입력명 (저장용)
    private String cnslTelno;      // 연락처 (재원생: MEMBER.USER_TELNO, 신규: 입력값)
    private String targetType;     // 구분 (재원생 / 신규문의)
    private String parentNm;       // 학부모명 (STUDENT.PRNT_USER_ID → MEMBER 조인)
    private String chrgUserId;     // 담당 강사 ID
    private String chrgNm;         // 담당 강사명 (MEMBER 조인)
    private String cnslTypeCd;     // 상담 유형 코드 (공통코드 211)
    private String cnslTypeNm;     // 상담 유형명 (공통코드 조인)
    private String cnslStatCd;     // 상담 상태 코드 (공통코드 212)
    private String cnslStatNm;     // 상담 상태명 (공통코드 조인)
    private LocalDateTime cnslDt;  // 상담 일시
    private String cnslCn;         // 상담 내용
    private String cnslSmry;       // 상담 요약
    private String fllwUpCn;       // 후속 조치 사항

    // ── 등록(저장) 시 사용 ──
    private String rgtrId;         // 등록자 ID
}
