package kr.or.ddit.finalProject.dto.pay.kakao;

import java.io.Serializable;
import lombok.Data;

@Data
public class KakaoPayApproveRequest implements Serializable {
    private final String cid = "TC0ONETIME"; // 테스트용 고정값
    private String cid_secret;
    private String tid;
    private String partner_order_id;
    private String partner_user_id;
    private String pg_token;
    private String payload;
    private Integer total_amount;
}
