package kr.or.ddit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import kr.or.ddit.finalProject.dto.user.UserDto;
import kr.or.ddit.finalProject.mapper.UserMapper;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDto user = userMapper.findByLoginId(username)
                .orElseThrow(() -> new UsernameNotFoundException("유저 없음: " + username));

        return null;
    }

}
