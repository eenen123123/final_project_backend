package kr.or.ddit.finalProject.dto.pay.kakao;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KakaoPayAmount implements Serializable {
    private int total; // 총 결제 금액
    private int tax_free; // 비과세 금액
    private int vat; // 부가세 금액
    private int point; // 포인트 금액
    private int discount; // 할인 금액
    private int green_deposit; // 친환경 포장재 보증금액
}
