package kr.or.ddit.finalProject.dto.tuition;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 미납자 관리 DTO
 * 학생별 TUITION_BILL(미납/연체) 집계 + MEMBER(학생명) + 최근 안내(NOTIFICATION)
 * ※ 학부모명/연락처는 학생↔학부모 매핑 테이블 부재로 현재 미연동(null)
 */
@Data
public class TuitionUnpaidDto {

    private String userId;            // 학생 ID
    private String studentNm;         // 학생명
    private String parentNm;          // 학부모명 (매핑 연동 시 채움)
    private String parentTel;         // 학부모 연락처 (매핑 연동 시 채움)
    private Long unpaidAmt;           // 미납액 합계 (미납+연체 청구 합)
    private Integer overdueCnt;       // 미납 회차 (누적 미납/연체 건수)
    private LocalDateTime lastNoticeDt; // 최근 안내 일시
}
