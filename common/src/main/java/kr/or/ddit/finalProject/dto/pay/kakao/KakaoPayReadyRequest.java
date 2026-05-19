package kr.or.ddit.finalProject.dto.pay.kakao;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 카카오페이 결제를 시작하기 위해 결제정보를 카카오페이 서버에 전달하고 결제 고유번호(TID)와 URL을 응답받는 단계입니다.
 * 
 *  Secret key를 헤더에 담아 파라미터 값들과 함께 POST로 요청합니다.
 * 
 *  요청이 성공하면 응답 바디에 JSON 객체로 다음 단계 진행을 위한 값들을 받습니다.
 * 
 *  서버(Server)는 tid를 저장하고, 클라이언트는 사용자 환경에 맞는 URL로 리다이렉트(redirect)합니다.
 */

@Data
@NoArgsConstructor
public class KakaoPayReadyRequest implements Serializable {
    private final String cid = "TC0ONETIME"; // 테스트용 고정값
    private String partner_order_id; // 가맹점 주문번호, 최대 100자
    private String partner_user_id; // 가맹점 회원 id, 최대 100자
    private String item_name; // 상품명, 최대 100자
    private int quantity; // 상품 수량
    private int total_amount; // 상품 총액
    private int tax_free_amount; // 상품 비과세 금액
    private String approval_url; // 결제 성공 시 redirect url, 최대 255자
    private String cancel_url; // 결제 취소 시 redirect url, 최대 255자
    private String fail_url; // 결제 실패 시 redirect url, 최대 255자

    // 응답값
    private String pg_token; // 결제 승인 요청 시 사용되는 토큰
    private String tid; // 결제 고유 번호
}
