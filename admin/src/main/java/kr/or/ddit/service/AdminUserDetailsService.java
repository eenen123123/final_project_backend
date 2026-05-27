package kr.or.ddit.service;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.dto.user.UserDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.user.UserException;
import kr.or.ddit.finalProject.mapper.MemberMapper;
import kr.or.ddit.finalProject.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AdminUserDetailsService implements UserDetailsService {

    @Autowired
    private MemberMapper memberMapper;



    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MemberDto user = memberMapper.findByUserId(username)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        // String role = user.getUserRole();
        // if (role.contains("ROLE_")) {
        //     role = role.replace("ROLE_", ""); // "ROLE_" 접두사 제거
        // }

        // UserDetails userDetails =
        //         User.builder().username(user.getUserId()).password(user.getUserEnpswd()).roles(role) // 관리자 권한 부여
        //                 .build();
        UserDetails userDetails = User.builder().username(user.getUserId())
                .password(user.getUserEnpswd()).authorities(user.getUserRole()).build();

        log.info("AdminUserDetailsService - loadUserByUsername: {}", userDetails);
        return userDetails;
    }

}
