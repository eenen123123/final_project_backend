package kr.or.ddit.finalProject.dto.pay.toss;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class TossPayRequest implements Serializable {
    private String paymentKey;
    private String orderId;
    private String amount;
}
