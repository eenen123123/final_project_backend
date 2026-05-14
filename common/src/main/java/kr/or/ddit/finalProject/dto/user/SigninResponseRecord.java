package kr.or.ddit.finalProject.dto.user;

/**
 * 로그인 응답 데이터를 담는 Record 클래스, 로그인 성공 시 Access Token과 Grant Type을 클라이언트에 전달하기 위해
 * 사용됨
 */
public record SigninResponseRecord(String grantType, String accessToken) {
}
