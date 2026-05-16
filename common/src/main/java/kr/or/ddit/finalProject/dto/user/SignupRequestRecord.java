package kr.or.ddit.finalProject.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 회원 가입 요청을 위한 DTO 클래스
 */
public record SignupRequestRecord(
        @NotBlank(message = "로그인 ID는 필수입니다.") @Size(min = 4, max = 20,
                message = "로그인 ID는 4자 이상 20자 이하로 입력해야 합니다.") String userId,

        @NotBlank(message = "비밀번호는 필수입니다.") @Size(min = 8, max = 20,
                message = "비밀번호는 8자 이상 20자 이하로 입력해야 합니다.") String password,

        @NotBlank(message = "이름은 필수입니다.") @Size(min = 2, max = 50,
                message = "이름은 2자 이상 50자 이하로 입력해야 합니다.") String name,
        @NotBlank(message = "닉네임은 필수입니다.") @Size(min = 2, max = 30,
                message = "닉네임은 2자 이상 30자 이하로 입력해야 합니다.") String nickName

) {
}
