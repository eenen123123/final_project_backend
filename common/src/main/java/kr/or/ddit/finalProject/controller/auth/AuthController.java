package kr.or.ddit.finalProject.controller.auth;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import kr.or.ddit.finalProject.dto.auth.AuthTokens;
import kr.or.ddit.finalProject.dto.user.SigninRequestRecord;
import kr.or.ddit.finalProject.dto.user.SigninResponseRecord;
import kr.or.ddit.finalProject.dto.user.SignupRequestRecord;
import kr.or.ddit.finalProject.dto.user.UserDto;
import kr.or.ddit.finalProject.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.Duration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequestRecord requestRecord) {
        try {
            userService.signup(requestRecord);
            return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }


    @PostMapping("/signin")
    public ResponseEntity<SigninResponseRecord> signin(
            @Valid @RequestBody SigninRequestRecord requestRecord) {

        AuthTokens tokens = userService.signin(requestRecord);
        ResponseCookie refreshCookie = createRefreshTokenCookie(tokens.refreshToken());
        return ResponseEntity.ok().header("Set-Cookie", refreshCookie.toString())
                .body(new SigninResponseRecord(tokens.grantType(), tokens.accessToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<SigninResponseRecord> refresh(
            @CookieValue(name = "refreshToken") String refreshToken) {
        try {
            AuthTokens tokens = userService.refresh(refreshToken);
            ResponseCookie refreshCookie = createRefreshTokenCookie(tokens.refreshToken());
            return ResponseEntity.ok().header("Set-Cookie", refreshCookie.toString())
                    .body(new SigninResponseRecord(tokens.grantType(), tokens.accessToken()));
        } catch (Exception e) {
            log.error("리프레시 토큰 처리 중 오류 발생 : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<Void> signout(@CookieValue(name = "refreshToken") String refreshToken) {
        userService.signout(refreshToken);
        ResponseCookie deleteCookie = deleteRefreshTokenCookie();
        return ResponseEntity.ok().header("Set-cookie", deleteCookie.toString()).build();
    }


    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(
            @RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효하지 않은 Authorization 헤더입니다");
        }

        UserDto user = userService.getUserByToken(authorizationHeader.substring(7));
        UserDto currentUser = UserDto.builder().loginId(user.getLoginId())
                .nickName(user.getNickName()).role(user.getRole()).build();

        return ResponseEntity.ok(currentUser);
    }



    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken).httpOnly(true).secure(false)
                .sameSite("Lax").path("/api/auth")
                .maxAge(Duration.ofMillis(userService.getRefreshTokenExpiration())).build();
    }

    private ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from("refreshToken", "").httpOnly(true).secure(false).sameSite("Lax")
                .path("/api/auth").maxAge(0).build();
    }



}
