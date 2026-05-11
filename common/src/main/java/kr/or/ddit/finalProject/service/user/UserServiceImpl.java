package kr.or.ddit.finalProject.service.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import kr.or.ddit.finalProject.dto.auth.AuthTokens;
import kr.or.ddit.finalProject.dto.user.RefreshTokenDto;
import kr.or.ddit.finalProject.dto.user.Role;
import kr.or.ddit.finalProject.dto.user.SigninRequestRecord;
import kr.or.ddit.finalProject.dto.user.SignupRequestRecord;
import kr.or.ddit.finalProject.dto.user.UserDto;
import kr.or.ddit.finalProject.dto.user.UserStatus;
import kr.or.ddit.finalProject.jwt.JwtTokenProvider;
import kr.or.ddit.finalProject.mapper.RefreshTokenMapper;
import kr.or.ddit.finalProject.mapper.UserMapper;
import kr.or.ddit.finalProject.util.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private static final String BEARER = "Bearer ";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenHashUtil tokenHashUtil;
    private final RefreshTokenMapper refreshTokenMapper;

    @Override
    public void signup(SignupRequestRecord signupRequest) {
        if (userMapper.existsByLoginId(signupRequest.loginId())) {
            throw new IllegalArgumentException("이미 사용중인 아이디 입니다.");
        }

        UserDto user = UserDto.builder().loginId(signupRequest.loginId())
                .password(passwordEncoder.encode(signupRequest.password()))
                .name(signupRequest.name()).nickName(signupRequest.nickName()).role(Role.ROLE_USER)
                .status(UserStatus.ACTIVE).build();
        userMapper.insertUser(user);
    }

    @Override
    public AuthTokens signin(SigninRequestRecord signinRequest) {
        UserDto user = authenticate(signinRequest);
        String accessToken = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getLoginId());
        upsertRefreshToken(user, refreshToken);
        return new AuthTokens(BEARER, accessToken, refreshToken);
    }

    @Override
    public void signout(String refreshToken) {
        String loginId = jwtTokenProvider.getLoginId(refreshToken);
        refreshTokenMapper.deleteRefreshToken(loginId);
    }

    @Override
    public long getRefreshTokenExpiration() {
        return jwtTokenProvider.getRefreshTokenExpiration();
    }

    @Override
    public void upsertRefreshToken(UserDto user, String refreshToken) {
        RefreshTokenDto refreshTokenDto = refreshTokenMapper.findByLoginId(user.getLoginId())
                .orElse(new RefreshTokenDto(user, tokenHashUtil.hmacToken(refreshToken),
                        jwtTokenProvider.getExpiration(refreshToken)));
        refreshTokenDto.rotate(tokenHashUtil.hmacToken(refreshToken),
                jwtTokenProvider.getExpiration(refreshToken));
        refreshTokenMapper.upsertRefreshToken(refreshTokenDto);
    }

    @Override
    public UserDto authenticate(SigninRequestRecord signinRequest) {
        String message = "아이디 또는 비밀번호가 일치하지 않습니다.";
        UserDto user = userMapper.findByLoginId(signinRequest.loginId())
                .orElseThrow(() -> new IllegalArgumentException(message));

        if (!passwordEncoder.matches(signinRequest.password(), user.getPassword())) {
            throw new IllegalArgumentException(message);
        }

        if (!user.getStatus().equals(UserStatus.ACTIVE)) {
            throw new IllegalArgumentException("현재 사용할 수 없는 계정입니다.");
        }
        return user;
    }

    @Override
    public UserDto getUserByToken(String token) {
        String loginId = jwtTokenProvider.getLoginId(token);
        return userMapper.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다."));
    }

    @Override
    public void changeRole(String loginId, Role newRole) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'changeRole'");
    }

    @Override
    public AuthTokens refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        if (!"refresh".equals(jwtTokenProvider.getTokenType(refreshToken))) {
            throw new IllegalArgumentException("리프레시 토큰이 아닙니다.");
        }

        String hashedToken = tokenHashUtil.hmacToken(refreshToken);
        RefreshTokenDto savedRefreshToken = refreshTokenMapper.findByToken(hashedToken)
                .orElseThrow(() -> new IllegalArgumentException("저장된 리프레시 토큰이 없습니다."));

        UserDto user = userMapper.findByLoginId(savedRefreshToken.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));

        String accessToken = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getLoginId());
        String newHashedToken = tokenHashUtil.hmacToken(newRefreshToken);
        savedRefreshToken.rotate(newHashedToken, jwtTokenProvider.getExpiration(newRefreshToken));
        refreshTokenMapper.upsertRefreshToken(savedRefreshToken);

        return new AuthTokens(BEARER, accessToken, newRefreshToken);
    }

}
