package kr.or.ddit.finalProject.service.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import kr.or.ddit.finalProject.dto.user.Role;
import kr.or.ddit.finalProject.dto.user.SignupRequestRecord;
import kr.or.ddit.finalProject.dto.user.UserDto;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@SpringBootTest
public class UserServiceTest {
    private final UserService userService;

}
