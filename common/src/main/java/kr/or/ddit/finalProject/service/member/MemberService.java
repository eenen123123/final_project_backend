package kr.or.ddit.finalProject.service.member;

import java.util.List;
import java.util.Map;
import kr.or.ddit.finalProject.dto.auth.AuthTokens;
import kr.or.ddit.finalProject.dto.member.AdminMemberDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.dto.user.SigninRequestRecord;
import kr.or.ddit.finalProject.dto.user.SignupRequestRecord;

/**
 * 회원 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 */
public interface MemberService {
    /**
     * 회원 가입 메서드
     * 
     * @param signupRequestRecord 회원 가입 요청 정보를 담은 SignupRequestRecord 객체
     * @return 회원 가입이 완료된 사용자 정보를 담은 MemberDto 객체
     */
    MemberDto signup(SignupRequestRecord signupRequestRecord);

    /**
     * 로그인 메서드
     * 
     * @param signinRequestRecord 로그인 요청 정보를 담은 SigninRequestRecord 객체
     * @return 인증 토큰을 담은 AuthTokens 객체
     */
    AuthTokens login(SigninRequestRecord signinRequestRecord);


    /**
     * 로그아웃 메서드
     * 
     * @param refreshToken 로그아웃할 사용자의 리프레시 토큰
     */
    void logout(String refreshToken);

    /**
     * JWT 토큰을 사용하여 사용자 정보를 조회하는 메서드
     * 
     * @return 리프레시 토큰의 만료 시간을 반환(밀리초 단위)
     */
    long getRefreshTokenExpiration();

    /**
     * 사용자의 리프레시 토큰을 저장하거나 업데이트하는 메서드
     * 
     * @param memberDto 리프레시 토큰을 저장할 사용자의 정보
     * @param refreshToken 저장할 리프레시 토큰
     */
    void upsertRefreshToken(MemberDto memberDto, String refreshToken);

    /**
     * 로그인 요청을 처리하여 사용자 정보를 반환하는 메서드
     * 
     * @param signinRequestRecord 로그인 요청 정보를 담은 SigninRequestRecord 객체
     * @return 인증된 사용자 정보를 담은 MemberDto 객체
     */
    MemberDto authenticate(SigninRequestRecord signinRequestRecord);

    /**
     * JWT 토큰을 사용하여 사용자 정보를 조회하는 메서드
     * 
     * @param token 사용자 정보를 조회할 JWT 토큰
     * @return 조회된 사용자 정보를 담은 MemberDto 객체
     */
    MemberDto getMemberByToken(String token);


    /**
     * 리프레시 토큰을 사용하여 새로운 인증 토큰을 발급하는 메소드
     * 
     * @param refreshToken 새로운 인증 토큰을 발급받기 위한 리프레시 토큰
     * @return 발급된 새로운 인증 토큰을 담은 AuthTokens 객체
     */
    AuthTokens reissueToken(String refreshToken);

    /**
     * 사용자 ID의 중복 여부를 확인하는 메서드
     * 
     * @param userId 중복 여부를 확인할 사용자 ID
     * @return 사용자 ID가 사용 가능한 경우 true, 이미 존재하는 경우 false
     */
    boolean isUserIdAvailable(String userId);

    MemberDto getMemberByUserId(String userId);

    AdminMemberDto getAdminUserById(String userId);

    /**
     * 비밀번호 확인 메서드
     *
     * @param userId 비밀번호를 확인할 사용자 ID
     * @param password 확인할 비밀번호 (평문)
     * @return 비밀번호가 일치하면 true, 일치하지 않으면 false
     */
    boolean verifyPassword(String userId, String password);

    /**
     * 회원 정보 수정 메서드
     *
     * @param memberDto 수정할 회원 정보를 담은 MemberDto 객체
     */
    void updateMember(MemberDto memberDto);

    void withdrawMember(String userId, String reason);

    Map<String, List<AdminMemberDto>> getGroupedAdminUsers(String currentUserId);

}
