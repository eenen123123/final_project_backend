package kr.or.ddit.finalProject.service.member;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import kr.or.ddit.finalProject.dto.auth.AuthTokens;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.dto.user.SigninRequestRecord;
import kr.or.ddit.finalProject.dto.user.SignupRequestRecord;
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


    @Override
    public void signup(SignupRequestRecord signupRequestRecord) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'signup'");
    }

    @Override
    public AuthTokens login(SigninRequestRecord signinRequestRecord) {
        MemberDto memberDto = authenticate(signinRequestRecord);

        return null;
    }

    @Override
    public void logout(String refreshToken) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'logout'");
    }

    @Override
    public long getRefreshTokenExpiration() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRefreshTokenExpiration'");
    }

    @Override
    public void upsertRefreshToken(MemberDto memberDto, String refreshToken) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'upsertRefreshToken'");
    }

    @Override
    public MemberDto authenticate(SigninRequestRecord signinRequestRecord) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MemberDto getMemberByToken(String token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMemberByToken'");
    }

    @Override
    public AuthTokens reissueToken(String refreshToken) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'reissueToken'");
    }

}
