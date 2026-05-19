package kr.or.ddit.finalProject.dto.pay.kakao;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KakaoPayApproveResponse implements Serializable {
    private final String cid = "TC0ONETIME"; // 테스트용 고정값
    private String aid; // 요청 고유 번호 - 승인/취소가 구분된 결제번호
    private String tid; // 결제 고유 번호 - 승인/취소가 동일한 결제번호
    private String sid; // 정기 결제용 ID, 정기 결제 CID로 단건 결제 요청 시 발급
    private String partner_order_id; // 가맹점 주문번호, 최대 100자
    private String partner_user_id; // 가맹점 회원 id, 최대 100자
    private String payment_method_type; // 결제 수단, CARD 또는 MONEY 중 하나
    private KakaoPayAmount amount; // 결제 금액 정보
    private KakaoPayCardInfo card_info; // 결제 상세 정보, 결제 수단이 카드일 경우만 포함
    private String item_name; // 상품 이름, 최대 100자
    private String item_code; // 상품 코드, 최대 100자
    private int quantity; // 상품 수량
    private String created_at; // 결제 준비 요청 시각
    private String approved_at; // 결제 승인 시각
    private String payload; // 결제 승인 요청에 대해 저장한 값, 요청 시 전달된 내용
}
