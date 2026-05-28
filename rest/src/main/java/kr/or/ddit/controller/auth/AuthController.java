package kr.or.ddit.controller.auth;

import java.time.Duration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import kr.or.ddit.finalProject.dto.auth.AuthTokens;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.dto.user.SigninRequestRecord;
import kr.or.ddit.finalProject.dto.user.SigninResponseRecord;
import kr.or.ddit.finalProject.dto.user.SignupRequestRecord;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.user.UserException;
import kr.or.ddit.finalProject.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {
    private final MemberService memberService;

    /**
     * 회원가입, SignupRequestRecord 객체를 받아서 회원가입을 처리함
     * 
     * @param requestRecord 회원가입 요청 데이터를 담은 객체
     * @return 회원가입 성공 시 201 Created 상태와 메시지, 실패 시 409 Conflict 상태와 에러 메시지
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequestRecord requestRecord) {
        memberService.signup(requestRecord);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 완료되었습니다.");

    }

    /**
     * <p>
     * 로그인, SigninRequestRecord 객체를 받아서 로그인 처리를 함
     * <p>
     * 로그인 성공 시 Access Token과 Refresh Token을 발급하고, Refresh Token은 HttpOnly 쿠키로 설정하여
     * 클라이언트에 전달함
     * 
     * @param requestRecord 로그인 요청 데이터를 담은 객체
     * @return 로그인 성공 시 200 OK 상태와 SigninResponseRecord 객체, 실패 시 401 Unauthorized
     *         상태로 반환
     */
    @PostMapping("/login")
    public ResponseEntity<SigninResponseRecord> signin(
            @Valid @RequestBody SigninRequestRecord requestRecord) {
        log.info("로그인 시도: {}", requestRecord.userId());

        AuthTokens tokens = memberService.login(requestRecord);
        ResponseCookie refreshCookie = createRefreshTokenCookie(tokens.refreshToken());
        return ResponseEntity.ok().header("Set-Cookie", refreshCookie.toString())
                .body(new SigninResponseRecord(tokens.grantType(), tokens.accessToken()));
    }

    /**
     * <p>
     * Access Token 갱신, 클라이언트에서 HttpOnly 쿠키로 전달된 Refresh Token을 받아서 Access Token을
     * 갱신함
     * 
     * @param refreshToken 클라이언트에서 전달된 Refresh Token
     * @return 갱신된 Access Token과 새로운 Refresh Token을 발급하여 200 OK 상태로 반환, 실패 시 401
     *         Unauthorized 상태로 반환
     */
    @PostMapping("/refresh")
    public ResponseEntity<SigninResponseRecord> refresh(
            @CookieValue(name = "refreshToken") String refreshToken) {
        log.info("리프레시 토큰 갱신 시도: {}", refreshToken);
        AuthTokens tokens = memberService.reissueToken(refreshToken);
        ResponseCookie refreshCookie = createRefreshTokenCookie(tokens.refreshToken());
        return ResponseEntity.ok().header("Set-Cookie", refreshCookie.toString())
                .body(new SigninResponseRecord(tokens.grantType(), tokens.accessToken()));

    }

    /**
     * <p>
     * 로그아웃, 클라이언트에서 HttpOnly 쿠키로 전달된 Refresh Token을 받아서 로그아웃 처리를 함
     * <p>
     * 서버에서는 해당 Refresh Token을 무효화하고, 클라이언트에서는 Refresh Token 쿠키를 삭제하여 로그아웃 상태로 만듦
     * 
     * @param refreshToken 클라이언트에서 전달된 Refresh Token
     * @return 로그아웃 성공 시 200 OK 상태로 반환, 실패 시 400 Bad Request 상태로 반환
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> signout(@CookieValue(name = "refreshToken") String refreshToken) {
        memberService.logout(refreshToken);
        ResponseCookie deleteCookie = deleteRefreshTokenCookie();
        return ResponseEntity.ok().header("Set-Cookie", deleteCookie.toString()).build();
    }

    /**
     * <p>
     * 현재 로그인한 사용자 정보 조회, 클라이언트에서 Authorization 헤더로 전달된 Access Token을 받아서 현재 로그인한
     * 사용자의 정보를 조회함
     * 
     * @param authorizationHeader 클라이언트에서 전달된 Authorization 헤더 (Bearer 토큰)
     * @return 현재 로그인한 사용자의 정보를 담은 MemberDto 객체를 200 OK 상태로 반환, 실패 시 400 Bad Request
     *         상태로 반환
     */
    @GetMapping("/me")
    public ResponseEntity<MemberDto> getCurrentUser(
            @RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UserException(ErrorCode.INVALID_TOKEN);
        }

        MemberDto user = memberService.getMemberByToken(authorizationHeader.substring(7));
        MemberDto currentUser = MemberDto.builder().userId(user.getUserId()).build();

        return ResponseEntity.ok(currentUser);
    }

    /**
     * <p>
     * Refresh Token 쿠키 생성, 로그인 또는 Access Token 갱신 시 새로운 Refresh Token을 발급받아서
     * HttpOnly 쿠키로 생성하여 반환함
     * 
     * @param refreshToken 새로운 Refresh Token 문자열
     * @return 생성된 HttpOnly 쿠키 객체, 클라이언트에서는 이 쿠키를 받아서 저장하여 이후 Access Token 갱신 요청 시
     *         사용함
     */
    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken).httpOnly(true).secure(false)
                .sameSite("Lax").path("/api/auth")
                .maxAge(Duration.ofMillis(memberService.getRefreshTokenExpiration())).build();
    }

    /**
     * <p>
     * Refresh Token 쿠키 삭제, 로그아웃 시 클라이언트에서 전달된 Refresh Token을 무효화하고
     * HttpOnly 쿠키를 삭제하여 로그아웃 상태로 만듦
     * 
     * @return 삭제된 HttpOnly 쿠키 객체, 클라이언트에서는 이 쿠키를 받아서 저장하여 이후 Access Token 갱신 요청 시
     *         사용하지 않음
     */
    private ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from("refreshToken", "").httpOnly(true).secure(false).sameSite("Lax")
                .path("/api/auth").maxAge(0).build();
    }

    @GetMapping("/test")
    public ResponseEntity<Void> authTest() {
        return ResponseEntity.ok(null);
    }

}
