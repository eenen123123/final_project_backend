package kr.or.ddit.finalProject.dto.email;

import java.io.Serializable;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 이메일 인증 정보를 담는 DTO 클래스
 * 
 * 회원 가입 시 이메일 인증을 위해 사용됩니다. 
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailVerificationDto implements Serializable {
    private Integer id;
    private String email;
    private String code;
    private LocalDate expiresAt;
    private LocalDate verifiedAt;
    private LocalDate createdAt;
}
