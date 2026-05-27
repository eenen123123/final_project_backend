package kr.or.ddit.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import kr.or.ddit.finalProject.dto.employee.EmployeeInfoDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.user.UserException;
import kr.or.ddit.finalProject.mapper.EmployeeMapper;
import kr.or.ddit.finalProject.mapper.MemberMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AdminUserDetailsService implements UserDetailsService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MemberDto user = memberMapper.findByUserId(username)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        EmployeeInfoDto employeeInfo = employeeMapper.selectEmployeeInfoByUserId(username);

        List<String> roles = new ArrayList<>();
        roles.add(user.getUserRole());
        roles.add(employeeInfo.getDeptCd());
        roles.add(employeeInfo.getJbgrCd());

        // admin, D100 (행정), A001 (행정짱)
        // String role = user.getUserRole();
        // if (role.contains("ROLE_")) {
        //     role = role.replace("ROLE_", ""); // "ROLE_" 접두사 제거
        // }
        // UserDetails userDetails =
        //         User.builder().username(user.getUserId()).password(user.getUserEnpswd()).roles(role) // 관리자 권한 부여
        //                 .build();
        UserDetails userDetails = User.builder().username(user.getUserId())
                .password(user.getUserEnpswd()).authorities(roles.stream().map(SimpleGrantedAuthority::new).toList()).build();
        userDetails.getAuthorities().forEach(auth -> log.info("Granted Authority: {}", auth.getAuthority()));
        return userDetails;
    }

}
