package kr.or.ddit.finalProject.dto.order;

public enum OrderStatus {
    PENDING, // 결제 대기
    PAID, // 결제 완료
    CANCELED, // 취소
    EXPIRED; // 결제 없이 방치되어 만료 (새 주문 생성 시 이전 PENDING 처리)
}
