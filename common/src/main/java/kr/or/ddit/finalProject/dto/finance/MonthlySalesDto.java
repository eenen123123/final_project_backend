package kr.or.ddit.finalProject.dto.finance;

import java.io.Serializable;

import lombok.Data;

/**
 * 매출 집계 (원비 수납 + 온라인 결제 합산)
 * · 월별 추이 행 또는 특정 월 요약으로 공용 사용
 * · 카테고리: 원비(tuition) / 강의(course) / 교재(textbook) / 기타(etc)
 */
@Data
public class MonthlySalesDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String mm; // 월 ("01"~"12") · 월 요약 시 null
    private long tuition; // 원비 (TUITION_PAYMENT 01)
    private long course; // 강의 (ORDER_ITEM COURSE)
    private long textbook; // 교재 (TUITION 02 + ORDER_ITEM TEXTBOOK)
    private long etc; // 기타 (TUITION 03 입회비·04 기타)
    private long total; // 총 매출
}
