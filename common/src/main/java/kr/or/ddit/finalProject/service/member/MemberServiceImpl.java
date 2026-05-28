package kr.or.ddit.finalProject.service.member;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import kr.or.ddit.finalProject.dto.auth.AuthTokens;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.dto.user.RefreshTokenDto;
import kr.or.ddit.finalProject.dto.user.SigninRequestRecord;
import kr.or.ddit.finalProject.dto.user.SignupRequestRecord;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.jwt.JwtTokenProvider;
import kr.or.ddit.finalProject.mapper.MemberMapper;
import kr.or.ddit.finalProject.mapper.RefreshTokenMapper;
import kr.or.ddit.finalProject.util.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenHashUtil tokenHashUtil;
    private final RefreshTokenMapper refreshTokenMapper;
    private static final String BEARER = "Bearer ";


    @Override
    public void signup(SignupRequestRecord signupRequestRecord) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'signup'");
    }

    @Override
    public AuthTokens login(SigninRequestRecord signinRequestRecord) {
        MemberDto memberDto = authenticate(signinRequestRecord);
        String accessToken = jwtTokenProvider.createAccessToken(memberDto.getUserId(),
                memberDto.getUserRole(), memberDto.getUserName());
        String refreshToken = jwtTokenProvider.createRefreshToken(memberDto.getUserId());
        upsertRefreshToken(memberDto, refreshToken);
        return new AuthTokens(BEARER, accessToken, refreshToken);
    }

    @Override
    public void logout(String refreshToken) {
        String userId = jwtTokenProvider.getUserId(refreshToken);
        refreshTokenMapper.deleteRefreshToken(userId);
    }

    @Override
    public long getRefreshTokenExpiration() {
        return jwtTokenProvider.getRefreshTokenExpiration();
    }

    @Override
    public void upsertRefreshToken(MemberDto memberDto, String refreshToken) {
        RefreshTokenDto refreshTokenDto = refreshTokenMapper.findByUserId(memberDto.getUserId())
                .orElse(new RefreshTokenDto(memberDto, tokenHashUtil.hmacToken(refreshToken),
                        jwtTokenProvider.getExpiration(refreshToken)));
        refreshTokenDto.rotate(tokenHashUtil.hmacToken(refreshToken),
                jwtTokenProvider.getExpiration(refreshToken));
        refreshTokenMapper.upsertRefreshToken(refreshTokenDto);
    }

    @Override
    public MemberDto authenticate(SigninRequestRecord signinRequestRecord) {
        MemberDto memberDto = memberMapper.findByUserId(signinRequestRecord.userId())
                .orElseThrow(() -> new FinalProjectException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(signinRequestRecord.userPswd(), memberDto.getUserEnpswd())) {
            throw new FinalProjectException(ErrorCode.USERNAME_OR_PASSWORD_INCORRECT);
        }
        return memberDto;
    }

    @Override
    public MemberDto getMemberByToken(String token) {
        String userId = jwtTokenProvider.getUserId(token);
        return memberMapper.findByUserId(userId)
                .orElseThrow(() -> new FinalProjectException(ErrorCode.INVALID_TOKEN));
    }

    @Override
    public AuthTokens reissueToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new FinalProjectException(ErrorCode.INVALID_TOKEN);
        }
        if (!"refresh".equals(jwtTokenProvider.getTokenType(refreshToken))) {
            throw new FinalProjectException(ErrorCode.INVALID_TOKEN);
        }

        String hashedToken = tokenHashUtil.hmacToken(refreshToken);
        RefreshTokenDto savedRefreshToken = refreshTokenMapper.findByToken(hashedToken)
                .orElseThrow(() -> new FinalProjectException(ErrorCode.INVALID_TOKEN));

        MemberDto memberDto = memberMapper.findByUserId(savedRefreshToken.getUserId())
                .orElseThrow(() -> new FinalProjectException(ErrorCode.INVALID_TOKEN));

        String accessToken = jwtTokenProvider.createAccessToken(memberDto.getUserId(),
                memberDto.getUserRole(), memberDto.getUserName());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(memberDto.getUserId());
        String newHashedToken = tokenHashUtil.hmacToken(newRefreshToken);
        savedRefreshToken.rotate(newHashedToken, jwtTokenProvider.getExpiration(newRefreshToken));
        refreshTokenMapper.upsertRefreshToken(savedRefreshToken);

        return new AuthTokens(BEARER, accessToken, newRefreshToken);

    }

}
