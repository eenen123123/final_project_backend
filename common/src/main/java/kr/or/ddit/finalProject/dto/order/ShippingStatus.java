package kr.or.ddit.finalProject.dto.order;

public enum ShippingStatus {
    READY, // 배송 준비중 
    SHIPPING, // 배송중
    DELIVERED, // 배송 완료
    CANCELED // 취소 (주문 취소/환불 처리 시)
}
