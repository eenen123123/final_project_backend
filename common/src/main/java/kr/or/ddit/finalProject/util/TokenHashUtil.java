package kr.or.ddit.finalProject.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 토큰 해싱 유틸리티, HMAC-SHA256 알고리즘을 사용하여 토큰을 해싱하는 기능을 제공함
 * 
 */
@Component
public class TokenHashUtil {

    private static final String ALGORITHM = "HmacSHA256";
    private final String SECRET_KEY;

    public TokenHashUtil(@Value("${app.token.secret-key}") String secretKey) {
        this.SECRET_KEY = secretKey;
    }

    public String hmacToken(String token) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(secretKeySpec);

            byte[] hashBytes = mac.doFinal(token.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (IllegalStateException e) {
            throw new RuntimeException("해시 알고리즘을 찾을 수 없습니다: " + ALGORITHM, e);
        } catch (Exception e) {
            throw new RuntimeException("토큰 해싱 중 오류가 발생했습니다.", e);
        }
    }
}
