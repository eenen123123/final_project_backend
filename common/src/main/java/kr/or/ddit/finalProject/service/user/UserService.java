package kr.or.ddit.finalProject.service.user;

import kr.or.ddit.finalProject.dto.auth.AuthTokens;
import kr.or.ddit.finalProject.dto.user.MemberRoleDto;
import kr.or.ddit.finalProject.dto.user.SigninRequestRecord;
import kr.or.ddit.finalProject.dto.user.SignupRequestRecord;
import kr.or.ddit.finalProject.dto.user.MemberDto;

public interface UserService {

    /**
     * 회원 가입 메서드
     * 
     * @param signupRequest 회원 가입 요청 정보를 담은 SignupRequestRecord 객체
     */
    void signup(SignupRequestRecord signupRequest);

    /**
     * 로그인 메서드
     * 
     * @param signinRequest 로그인 요청 정보를 담은 SigninRequestRecord 객체
     * @return 인증 토큰을 담은 AuthTokens 객체
     */
    AuthTokens signin(SigninRequestRecord signinRequest);

    /**
     * 로그아웃 메서드
     * 
     * @param refreshToken 로그아웃할 사용자의 리프레시 토큰
     */
    void signout(String refreshToken);

    /**
     * 리프레시 토큰의 만료 시간을 반환하는 메서드
     * 
     * @return 리프레시 토큰의 만료 시간 (밀리초 단위)
     */
    long getRefreshTokenExpiration();

    /**
     * 사용자의 리프레시 토큰을 저장하거나 업데이트하는 메서드
     * 
     * @param user         리프레시 토큰을 저장하거나 업데이트할 사용자 정보
     * @param refreshToken 저장하거나 업데이트할 리프레시 토큰
     */
    void upsertRefreshToken(MemberDto user, String refreshToken);

    /**
     * 로그인 요청을 처리하여 사용자 정보를 반환하는 메서드
     * 
     * @param signinRequest 로그인 요청 정보를 담은 SigninRequestRecord 객체
     * @return 인증된 사용자 정보를 담은 UserDto 객체
     */
    MemberDto authenticate(SigninRequestRecord signinRequest);

    /**
     * JWT 토큰을 사용하여 사용자 정보를 조회하는 메서드
     * 
     * @param token JWT 토큰
     * @return JWT 토큰에 해당하는 사용자 정보를 담은 UserDto 객체
     */
    MemberDto getUserByToken(String token);

    /**
     * 사용자의 역할을 변경하는 메서드
     * 
     * @param userId 변경할 사용자의 로그인 ID
     * @param newRole 새로운 역할
     */
    void changeRole(String userId, MemberRoleDto newRole);

    /**
     * 리프레시 토큰을 사용하여 새로운 인증 토큰을 발급하는 메서드
     * 
     * @param refreshToken 리프레시 토큰
     * @return 새로운 인증 토큰을 담은 AuthTokens 객체
     */
    AuthTokens refresh(String refreshToken);

}
