package kr.or.ddit.finalProject.service.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import kr.or.ddit.finalProject.dto.auth.AuthTokens;
import kr.or.ddit.finalProject.dto.user.Role;
import kr.or.ddit.finalProject.dto.user.SigninRequestRecord;
import kr.or.ddit.finalProject.dto.user.SignupRequestRecord;
import kr.or.ddit.finalProject.dto.user.UserDto;
import kr.or.ddit.finalProject.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void signup(SignupRequestRecord signupRequest) {
        if (userMapper.existsByLoginId(signupRequest.loginId())) {
            throw new IllegalArgumentException("이미 사용중인 아이디 입니다.");
        }
        // TODO Nickname 확인하고 insert할 때 같이 추가하게 변경해야함

        UserDto user = UserDto.builder().loginId(signupRequest.loginId())
                .password(passwordEncoder.encode(signupRequest.password())).role(Role.ROLE_USER)
                .build();
        userMapper.insertUser(user);
    }



    @Override
    public AuthTokens signin(SigninRequestRecord signinRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'signin'");
    }

    @Override
    public void signout(String refreshToken) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'signout'");
    }

    @Override
    public long getRefreshTokenExpiration() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRefreshTokenExpiration'");
    }

    @Override
    public void upsertRefreshToken(UserDto user, String refreshToken) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'upsertRefreshToken'");
    }

    @Override
    public UserDto authenticate(SigninRequestRecord signinRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'authenticate'");
    }

    @Override
    public UserDto getUserByToken(String token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserByToken'");
    }

    @Override
    public void changeRole(String loginId, Role newRole) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'changeRole'");
    }

    @Override
    public AuthTokens refresh(String refreshToken) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'refresh'");
    }

}
