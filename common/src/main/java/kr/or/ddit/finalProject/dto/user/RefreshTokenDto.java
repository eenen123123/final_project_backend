package kr.or.ddit.finalProject.dto.user;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshTokenDto {

    private Long id; // DB에서 자동 생성되는 ID

    private String loginId; // RefreshToken과 연관된 User의 로그인 ID

    private UserDto user; // RefreshToken과 연관된 User 정보

    private String token; // 실제 Refresh Token 문자열

    private Instant expiredAt; // Refresh Token의 만료 시간


    public RefreshTokenDto(UserDto user, String token, Instant expiredAt) {
        this.user = user;
        this.loginId = user.getLoginId();
        this.token = token;
        this.expiredAt = expiredAt;
    }

    public void rotate(String token, Instant expiredAt) {
        this.token = token;
        this.expiredAt = expiredAt;
    }
}
