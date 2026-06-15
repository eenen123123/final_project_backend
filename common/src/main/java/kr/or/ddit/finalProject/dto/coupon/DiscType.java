package kr.or.ddit.finalProject.dto.coupon;

/*
    * 할인 방식 (FIXED:정액 / RATE:정률)
    * - FIXED: 상품 가격에서 일정 금액을 할인하는 방식
    * - RATE: 상품 가격의 일정 비율을 할인하는 방식
*/

public enum DiscType {
    FIXED, // 정액 할인
    RATE;  // 정률 할인
}
