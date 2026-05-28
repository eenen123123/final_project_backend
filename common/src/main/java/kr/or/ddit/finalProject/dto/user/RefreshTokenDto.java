package kr.or.ddit.finalProject.dto.user;

import java.time.Instant;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshTokenDto {

    private Long id; // DB에서 자동 생성되는 ID

    private String userId; // RefreshToken과 연관된 User의 로그인 ID

    private MemberDto memberDto; // RefreshToken과 연관된 User 정보

    private String token; // 실제 Refresh Token 문자열

    private Instant expiredAt; // Refresh Token의 만료 시간

    public RefreshTokenDto(MemberDto memberDto, String token, Instant expiredAt) {
        this.memberDto = memberDto;
        this.userId = memberDto.getUserId();
        this.token = token;
        this.expiredAt = expiredAt;
    }

    public void rotate(String token, Instant expiredAt) {
        this.token = token;
        this.expiredAt = expiredAt;
    }
}
