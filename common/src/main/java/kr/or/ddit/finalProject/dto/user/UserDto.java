package kr.or.ddit.finalProject.dto.user;

import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto implements Serializable {

    @NotBlank
    @Size(min = 4, max = 20, message = "로그인 ID는 4자 이상 20자 이하로 입력해야 합니다.")
    private String loginId;

    @NotBlank
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해야 합니다.")
    private String password;

    @NotBlank
    @Size(min = 2, max = 20, message = "이름은 2자 이상 20자 이하로 입력해야 합니다.")
    private String name;

    @NotBlank
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해야 합니다.")
    private String nickName;

    private Role role; // 사용자 역할 (예: USER, ADMIN)

    private UserStatus status; // 사용자 상태 (예: ACTIVE, INACTIVE)
}
