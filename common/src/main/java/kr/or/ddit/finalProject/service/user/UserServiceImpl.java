package kr.or.ddit.finalProject.service.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import kr.or.ddit.finalProject.dto.auth.AuthTokens;
import kr.or.ddit.finalProject.dto.user.MemberRoleDto;
import kr.or.ddit.finalProject.dto.user.RefreshTokenDto;
import kr.or.ddit.finalProject.dto.user.SigninRequestRecord;
import kr.or.ddit.finalProject.dto.user.SignupRequestRecord;
import kr.or.ddit.finalProject.dto.user.MemberDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.user.UserException;
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
        if (userMapper.existsByUserId(signupRequest.userId())) {
            throw new UserException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        MemberDto user = MemberDto.builder().userId(signupRequest.userId())
                .userEnpswd(passwordEncoder.encode(signupRequest.password()))
                .userNm(signupRequest.name()).userRole("ROLE_USER").build();
        userMapper.insertUser(user);
    }

    @Override
    public AuthTokens signin(SigninRequestRecord signinRequest) {
        MemberDto user = authenticate(signinRequest);
        log.info("Authenticated user: {}", user.getUserId());
        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(),
                user.getUserRole(), user.getUserNm());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());
        upsertRefreshToken(user, refreshToken);
        return new AuthTokens(BEARER, accessToken, refreshToken);
    }

    @Override
    public void signout(String refreshToken) {
        String userId = jwtTokenProvider.getUserId(refreshToken);
        refreshTokenMapper.deleteRefreshToken(userId);
    }

    @Override
    public long getRefreshTokenExpiration() {
        return jwtTokenProvider.getRefreshTokenExpiration();
    }

    @Override
    public void upsertRefreshToken(MemberDto user, String refreshToken) {
        RefreshTokenDto refreshTokenDto = refreshTokenMapper.findByUserId(user.getUserId())
                .orElse(new RefreshTokenDto(user, tokenHashUtil.hmacToken(refreshToken),
                        jwtTokenProvider.getExpiration(refreshToken)));
        refreshTokenDto.rotate(tokenHashUtil.hmacToken(refreshToken),
                jwtTokenProvider.getExpiration(refreshToken));
        refreshTokenMapper.upsertRefreshToken(refreshTokenDto);
    }

    @Override
    public MemberDto authenticate(SigninRequestRecord signinRequest) {
        MemberDto user = userMapper.findByUserId(signinRequest.userId())
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(signinRequest.userPswd(), user.getUserEnpswd())) {
            throw new UserException(ErrorCode.USERNAME_OR_PASSWORD_INCORRECT);
        }

        // if (!user.getUserStatus().equals(UserStatus.ACTIVE)) {
        // throw new UserException(ErrorCode.ACCOUNT_UNUSABLE);
        // }
        return user;
    }

    @Override
    public MemberDto getUserByToken(String token) {
        String userId = jwtTokenProvider.getUserId(token);
        return userMapper.findByUserId(userId)
                .orElseThrow(() -> new UserException(ErrorCode.INVALID_TOKEN));
    }

    @Override
    public void changeRole(String userId, MemberRoleDto newRole) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'changeRole'");
    }

    @Override
    public AuthTokens refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UserException(ErrorCode.INVALID_TOKEN);
        }

        if (!"refresh".equals(jwtTokenProvider.getTokenType(refreshToken))) {
            throw new UserException(ErrorCode.INVALID_TOKEN);
        }

        String hashedToken = tokenHashUtil.hmacToken(refreshToken);
        RefreshTokenDto savedRefreshToken = refreshTokenMapper.findByToken(hashedToken)
                .orElseThrow(() -> new UserException(ErrorCode.INVALID_TOKEN));

        MemberDto user = userMapper.findByUserId(savedRefreshToken.getUserId())
                .orElseThrow(() -> new UserException(ErrorCode.INVALID_TOKEN));

        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(),
                user.getUserRole(), user.getUserNm());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());
        String newHashedToken = tokenHashUtil.hmacToken(newRefreshToken);
        savedRefreshToken.rotate(newHashedToken, jwtTokenProvider.getExpiration(newRefreshToken));
        refreshTokenMapper.upsertRefreshToken(savedRefreshToken);

        return new AuthTokens(BEARER, accessToken, newRefreshToken);
    }

}
