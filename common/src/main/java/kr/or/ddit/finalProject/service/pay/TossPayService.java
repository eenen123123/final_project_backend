package kr.or.ddit.finalProject.service.pay;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import kr.or.ddit.finalProject.dto.pay.toss.TossPayRequest;
import kr.or.ddit.finalProject.dto.pay.toss.TossPaymentResponse;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.util.PrintPrettyObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TossPayService {

    @Value("${toss.pay.secret.key}")
    private String TOSS_PAY_SECRET_KEY;
    private final String CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";

    private final RestTemplate restTemplate;

    public TossPaymentResponse confirm(TossPayRequest request) {

        try {

            String encoded = Base64.getEncoder()
                    .encodeToString((TOSS_PAY_SECRET_KEY + ":").getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + encoded);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<TossPayRequest> entity = new HttpEntity<>(request, headers);

            TossPaymentResponse response =
                    restTemplate.postForObject(CONFIRM_URL, entity, TossPaymentResponse.class);
            String prettyResponse = PrintPrettyObject.toPrettyString(response);
            log.info("Toss Pay confirm response: {}", prettyResponse);
            return response;
        } catch (HttpClientErrorException e) {
            log.error("Toss Pay confirm failed [{}]: {}", e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
    }
}
