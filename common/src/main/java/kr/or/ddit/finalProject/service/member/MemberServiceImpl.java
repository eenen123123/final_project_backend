package kr.or.ddit.finalProject.service.member;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kr.or.ddit.finalProject.dto.auth.AuthTokens;
import kr.or.ddit.finalProject.dto.email.EmailVerificationDto;
import kr.or.ddit.finalProject.dto.member.AdminMemberDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.dto.user.RefreshTokenDto;
import kr.or.ddit.finalProject.dto.user.SigninRequestRecord;
import kr.or.ddit.finalProject.dto.user.SignupRequestRecord;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.jwt.JwtTokenProvider;
import kr.or.ddit.finalProject.mapper.BlacklistMapper;
import kr.or.ddit.finalProject.mapper.MemberMapper;
import kr.or.ddit.finalProject.mapper.RefreshTokenMapper;
import kr.or.ddit.finalProject.mapper.email.EmailVerificationMapper;
import kr.or.ddit.finalProject.util.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberMapper memberMapper;
    private final EmailVerificationMapper emailVerificationMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenHashUtil tokenHashUtil;
    private final BlacklistMapper blacklistMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private static final String BEARER = "Bearer ";


    @Override
    @Transactional
    public MemberDto signup(SignupRequestRecord signupRequestRecord) {

        // id 다시 한번 체크
        String userId = signupRequestRecord.userId();
        if (!memberMapper.findByUserId(userId).isEmpty()) {
            throw new FinalProjectException(ErrorCode.USER_ID_ALREADY_EXISTS);
        }

        // 이메일 인증 됐는지 체크

        String emailAddr = signupRequestRecord.emailAddr();

        EmailVerificationDto emailVerificationDto =
                emailVerificationMapper.isEmailVerified(emailAddr);
        if (emailVerificationDto == null) {
            throw new FinalProjectException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        // 생년월일과 주민등록번호, 성별이 일치하는지 체크
        LocalDate birthDate = signupRequestRecord.birthDate();
        String userEnrno = signupRequestRecord.userEnrno();
        String gender = signupRequestRecord.gender();

        if (!isBirthDateMatchingEnrno(birthDate, userEnrno)) {
            throw new FinalProjectException(ErrorCode.BIRTHDATE_ENRNO_MISMATCH);
        }

        char enrnoGenderDigit = userEnrno.charAt(7);
        String enrnoGender = (enrnoGenderDigit == '1' || enrnoGenderDigit == '3') ? "M" : "F";
        if (!enrnoGender.equalsIgnoreCase(gender)) {
            throw new FinalProjectException(ErrorCode.GENDER_MISMATCH);
        }



        // 회원 가입 처리
        MemberDto memberDto = new MemberDto();
        memberDto.setUserId(signupRequestRecord.userId());
        memberDto.setUserEnpswd(passwordEncoder.encode(signupRequestRecord.password()));
        memberDto.setUserName(signupRequestRecord.name());
        memberDto.setUserEmailAddr(signupRequestRecord.emailAddr());
        memberDto.setUserTelno(signupRequestRecord.telno().replaceAll("-", ""));
        memberDto.setUserZip(signupRequestRecord.zip());
        memberDto.setUserAddr(signupRequestRecord.addr());
        memberDto.setUserDaddr(signupRequestRecord.daddr());
        memberDto.setUserGndrCd(signupRequestRecord.gender());
        memberDto.setUserBrdt(signupRequestRecord.birthDate());
        memberDto.setUserEnrrno(passwordEncoder.encode(signupRequestRecord.userEnrno()));

        memberMapper.insertMember(memberDto);

        // 이메일 인증 정보 삭제
        emailVerificationMapper.deleteEmailVerificationByEmail(emailAddr);


        return memberDto;
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
        log.info("enable :  {}", memberDto.getEnable());
        if (memberDto.getEnable().equals("N")) {
            throw new FinalProjectException(ErrorCode.ACCOUNT_UNUSABLE);
        }
        if (!passwordEncoder.matches(signinRequestRecord.userPswd(), memberDto.getUserEnpswd())) {
            throw new FinalProjectException(ErrorCode.USERNAME_OR_PASSWORD_INCORRECT);
        }
        // 주의 학생 정지 차단: 현재 적용 중인 정지(영구 또는 미만료)면 로그인 거부.
        // MEMBER 는 건드리지 않고 STUDENT_BLACK_LIST 를 조회해 판정한다(만료는 END_DT 비교로 자동 해제).
        if (blacklistMapper.countActiveBlock(memberDto.getUserId()) > 0) {
            java.time.LocalDateTime endDt = blacklistMapper.selectActiveBlockEndDt(memberDto.getUserId());
            String msg = (endDt == null)
                    ? "영구정지된 계정입니다. 관리자에게 문의하세요."
                    : "정지된 계정입니다. " + endDt.toLocalDate() + " 까지 로그인할 수 없습니다.";
            throw new FinalProjectException(ErrorCode.ACCOUNT_SUSPENDED, msg);
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

    @Override
    public boolean isUserIdAvailable(String userId) {
        return memberMapper.findByUserId(userId).isEmpty();
    }


    private boolean isBirthDateMatchingEnrno(LocalDate birthDate, String userEnrno) {
        if (userEnrno.length() != 14 || userEnrno.charAt(6) != '-') {
            return false; // 주민등록번호 형식이 올바르지 않은 경우
        }

        String enrnoBirthDatePart = userEnrno.substring(0, 6);
        String birthDateStr =
                birthDate.format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));

        return enrnoBirthDatePart.equals(birthDateStr);
    }

    @Override
    public MemberDto getMemberByUserId(String userId) {
        return memberMapper.findByUserId(userId)
                .orElseThrow(() -> new FinalProjectException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public AdminMemberDto getAdminUserById(String userId) {
        return memberMapper.getAdminUserById(userId);
    }

    @Override
    public boolean verifyPassword(String userId, String password) {
        if (password == null || password.isBlank()) {
            throw new FinalProjectException(ErrorCode.INVALID_PASSWORD);
        }
        MemberDto memberDto = memberMapper.findByUserId(userId)
                .orElseThrow(() -> new FinalProjectException(ErrorCode.USER_NOT_FOUND));
        return passwordEncoder.matches(password, memberDto.getUserEnpswd());
    }

    @Override
    @Transactional
    public void updateMember(MemberDto memberDto) {
        if (memberDto.getUserEnpswd() != null && !memberDto.getUserEnpswd().isBlank()) {
            memberDto.setUserEnpswd(passwordEncoder.encode(memberDto.getUserEnpswd()));
        }
        memberMapper.updateMember(memberDto);
    }

    @Override
    public Map<String, List<AdminMemberDto>> getGroupedAdminUsers(String currentUserId) {
        List<AdminMemberDto> adminUsers = memberMapper.getAdminUsers(currentUserId);
        Map<String, List<AdminMemberDto>> groupedAdminUsers = adminUsers.stream()
                .collect(Collectors.groupingBy(adminUser -> adminUser.getEmployeeInfo().getDeptNm(),
                        LinkedHashMap::new, Collectors.toList()));
        return groupedAdminUsers;
    }


}
