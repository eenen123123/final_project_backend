package kr.or.ddit.finalProject.dto.pay.kakao;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KakaoPayReadyResponse implements Serializable {
    private String tid; // 결제 고유 번호, 20자 
    private String next_redirect_pc_url; // PC 웹에서 결제 승인 요청 시 이동할 URL, 최대 255자
    private String next_redirect_mobile_url; // 모바일 웹에서 결제 승인 요청 시 이동할 URL, 최대 255자
    private String next_redirect_app_url; // 앱에서 결제 승인 요청 시 이동할 URL, 최대 255자
    private LocalDateTime created_at; // 결제 준비 요청 시간

    private String partner_order_id; // 주문 번호, 최대 100자
}
