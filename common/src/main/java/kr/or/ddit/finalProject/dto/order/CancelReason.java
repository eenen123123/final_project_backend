package kr.or.ddit.finalProject.dto.order;

public enum CancelReason {
    CHANGE_OF_MIND,  // 단순 변심
    DELIVERY_DELAY,  // 배송 지연
    WRONG_ORDER,     // 주문 실수
    DUPLICATE_ORDER, // 중복 주문
    OTHER;           // 기타
}
