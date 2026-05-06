package kr.or.ddit.finalProject.dto.auth;

/**
 * 인증 토큰을 담는 DTO 클래스
 */
public record AuthTokens(String grantType, String accessToken, String refreshToken) {
}
