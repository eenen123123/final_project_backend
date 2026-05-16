package kr.or.ddit.finalProject.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtTokenProvider {

    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    // application.properties에서 JWT 설정을 가져옴
    @Value("${jwt.secret}")
    private String secretKey;

    // access token의 유효 기간
    @Value("${jwt.accessTokenExpiration}")
    private long accessTokenExpiration;

    // refresh token의 유효 기간
    @Value("${jwt.refreshTokenExpiration}")
    private long refreshTokenExpiration;

    // JWT 서명을 위한 SecretKey 객체
    private SecretKey key;

    // 애플리케이션이 시작될 때 SecretKey 객체를 초기화
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // 로그인 성공 시 JWT access token을 생성하는 메소드
    public String createAccessToken(String userId, String role, String userName) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder().subject(userId).claim("type", ACCESS_TOKEN_TYPE).claim("role", role)
                .claim("userName", userName).issuedAt(now).expiration(expiration).signWith(key)
                .compact();
    }

    /**
     * 로그인 성공 시 JWT refresh token을 생성하는 메소드
     * 
     * @param userId 사용자의 ID
     * @return 생성된 JWT refresh token
     */
    public String createRefreshToken(String userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder().subject(userId).claim("type", REFRESH_TOKEN_TYPE).issuedAt(now)
                .expiration(expiration).signWith(key).compact();
    }

    // JWT 토큰에서 사용자 ID를 추출하는 메소드
    public String getUserId(String token) {
        return parseClaims(token).getSubject();
    }

    // JWT 토큰에서 사용자 역할을 추출하는 메소드
    public String getRole(String token) {
        String role = parseClaims(token).get("role", String.class);
        return role;
    }

    // JWT 토큰에서 토큰 유형(access 또는 refresh)을 추출하는 메소드
    public String getTokenType(String token) {
        return parseClaims(token).get("type", String.class);
    }

    // refresh token의 유효 기간을 반환하는 메소드
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    // JWT 토큰에서 만료 시간을 추출하는 메소드
    public Instant getExpiration(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    // JWT 토큰의 유효성을 검증하는 메소드
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // JWT 토큰에서 클레임을 파싱하는 메소드
    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
