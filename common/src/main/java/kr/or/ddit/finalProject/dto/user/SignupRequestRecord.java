package kr.or.ddit.finalProject.dto.user;

import java.time.LocalDate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 회원 가입 요청을 위한 DTO 클래스
 */
public record SignupRequestRecord(

        @NotBlank(message = "ID는 필수입니다.")
        @Size(min = 4, max = 20, message = " ID는 4자 이상 20자 이하로 입력해야 합니다.")
        String userId,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해야 합니다.")
        String password,

        @NotBlank(message = "이름은 필수입니다.")
        @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하로 입력해야 합니다.")
        String name,

        String gender,

        @NotBlank(message = "생년월일은 필수입니다.")
        @Past(message = "생년월일은 과거 날짜여야 합니다.")
        LocalDate birthDate,

        @NotBlank(message = "전화번호는 필수입니다.")
        @Size(min = 10, max = 15, message = "전화번호는 10자 이상 15자 이하로 입력해야 합니다.")
        @Pattern(regexp = "^010-\\d{3,4}-\\d{4}$", message = "전화번호는 010-0000-0000 형식이어야 합니다.")
        String telno,

        @NotBlank(message = "이메일 주소는 필수입니다.")
        @Email(message = "유효한 이메일 주소를 입력해야 합니다.")
        @Size(max = 100, message = "이메일 주소는 100자 이하로 입력해야 합니다.")
        String emailAddr,

        @NotBlank(message = "우편번호는 필수입니다.")
        @Size(min = 5, max = 10, message = "우편번호는 5자 이상 10자 이하로 입력해야 합니다.")
        String zip,


        @NotBlank(message = "주소는 필수입니다.")
        @Size(max = 200, message = "주소는 200자 이하로 입력해야 합니다.")
        String addr,

        @Size(max = 200, message = "상세 주소는 200자 이하로 입력해야 합니다.")
        String daddr) {

}
