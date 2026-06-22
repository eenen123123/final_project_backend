package kr.or.ddit.finalProject.dto.order;

public enum OrderStatus {
    PENDING,           // 결제 대기
    PAID,              // 결제 완료
    CANCEL_REQUESTED,  // 취소/환불 요청 (처리중)
    CANCELED,          // 취소/환불 완료
    EXPIRED;           // 결제 없이 방치되어 만료
}
