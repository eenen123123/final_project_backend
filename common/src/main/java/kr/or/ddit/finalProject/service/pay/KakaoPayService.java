package kr.or.ddit.finalProject.service.pay;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import kr.or.ddit.finalProject.dto.pay.kakao.KakaoPayApproveRequest;
import kr.or.ddit.finalProject.dto.pay.kakao.KakaoPayApproveResponse;
import kr.or.ddit.finalProject.dto.pay.kakao.KakaoPayReadyRequest;
import kr.or.ddit.finalProject.dto.pay.kakao.KakaoPayReadyResponse;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoPayService {

    // TODO : 실제 서비스에서는 DB에 저장하는 방식으로 변경 필요 (동시성 문제, 서버 재시작 시 데이터 손실 문제 등 고려)

    @Value("${kakao.pay.secret.key}")
    private String KAKAO_PAY_SECRET_KEY;

    private static final String DEFAULT_URL = "http://localhost:8081/api/test/kakao-pay";

    private static final String KAKAO_PAY_READY_URL =
            "https://open-api.kakaopay.com/online/v1/payment/ready";
    private static final String KAKAO_PAY_APPROVE_URL =
            "https://open-api.kakaopay.com/online/v1/payment/approve";

    private ConcurrentHashMap<String, Map<String, String>> paymentDataStore =
            new ConcurrentHashMap<>();

    private final RestTemplate restTemplate;

    /**
     * 카카오페이 결제 준비 요청을 보내고, 결제 승인 URL을 반환하는 메서드
     * 
     * @param request 결제 준비 요청 데이터 (상품명, 금액, 수량)
     * @param authentication 현재 인증된 사용자 정보
     * @return 결제 승인 URL
     */
    public KakaoPayReadyResponse payReady(KakaoPayReadyRequest request,
            Authentication authentication) {
        String uuid = UUID.randomUUID().toString();


        String approvalUrl = DEFAULT_URL + "/success/" + uuid;
        String cancelUrl = DEFAULT_URL + "/cancel/" + uuid;
        String failUrl = DEFAULT_URL + "/fail/" + uuid;

        String userId = authentication.getPrincipal().toString();
        String partner_order_id = "order123"; // 실제 주문 ID로 대체해야 함

        request.setPartner_order_id(partner_order_id);
        request.setPartner_user_id(userId);
        request.setTax_free_amount(0); // 비과세 금액은 0으로 설정
        request.setApproval_url(approvalUrl);
        request.setCancel_url(cancelUrl);
        request.setFail_url(failUrl);

        KakaoPayReadyResponse response = sendPostRequest(KAKAO_PAY_READY_URL, request,
                KakaoPayReadyResponse.class, ErrorCode.KAKAO_PAY_READY_FAILED);
        response.setPartner_order_id(partner_order_id);
        paymentDataStore.put(uuid, Map.of("partner_order_id", partner_order_id, "partner_user_id",
                userId, "tid", response.getTid()));
        return response;
    }

    /**
     * 카카오페이 결제 승인 요청을 보내고, 결제 승인 결과를 반환하는 메서드
     * 
     * @param pgToken 카카오페이에서 결제 승인 후 전달받는 토큰
     * @param uuid 결제 준비 단계에서 생성된 UUID로, 결제 승인 시 필요한 데이터 조회에 사용
     * @return 카카오페이 결제 승인 결과 데이터 (결제 상태, 결제 금액 등)
     */
    public KakaoPayApproveResponse approvePayment(String pgToken, String uuid) {
        KakaoPayApproveRequest approveRequest = new KakaoPayApproveRequest();

        Map<String, String> paymentData = paymentDataStore.get(uuid);
        String partner_order_id = paymentData.get("partner_order_id");
        String partner_user_id = paymentData.get("partner_user_id");
        String tid = paymentData.get("tid");

        approveRequest.setTid(tid);
        approveRequest.setPartner_user_id(partner_user_id);
        approveRequest.setPartner_order_id(partner_order_id);
        approveRequest.setPg_token(pgToken);

        KakaoPayApproveResponse response = sendPostRequest(KAKAO_PAY_APPROVE_URL, approveRequest,
                KakaoPayApproveResponse.class, ErrorCode.KAKAO_PAY_APPROVE_FAILED);

        removePaymentData(uuid);
        return response;
    }

    /**
     * 카카오페이 API에 POST 요청을 보내는 공통 메서드
     * 
     * @param <T> 응답 타입
     * @param url 요청을 보낼 카카오페이 API URL
     * @param requestObject 요청에 포함될 데이터 객체 (결제 준비 요청 또는 결제 승인 요청)
     * @param responseType  응답으로 받을 데이터 타입 클래스
     * @param errorCode API 요청 실패 시 사용할 에러 코드
     * @return 카카오페이 API로부터 받은 응답 데이터 객체
     */
    private <T> T sendPostRequest(String url, Object requestObject, Class<T> responseType,
            ErrorCode errorCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "DEV_SECRET_KEY " + KAKAO_PAY_SECRET_KEY);
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");

        HttpEntity<Object> entity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<T> response = restTemplate.postForEntity(url, entity, responseType);
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new FinalProjectException(errorCode);
        }
    }

    public void cancelPayment(String uuid) {
        removePaymentData(uuid);
    }

    public void failPayment(String uuid) {
        removePaymentData(uuid);
    }

    // TODO : 사용자가 결제 창을 그냥 닫아버리면 해당 uuid 데이터가 남음 -> timestamp 추가 후 주기적 만료 처리 등의 방법으로 개선 필요
    // TODO : 요청/응답에 쓰인 객체들 DB의 컬럼 명에 맞추기
    private void removePaymentData(String uuid) {
        paymentDataStore.remove(uuid);
    }
}
