package kr.or.ddit.finalProject.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDto {
    private String userId; // 사용자ID (PK)
    private String userName; // 사용자명
    private String userRoleCd; // 권한코드 (ROLE_ADMIN, ROLE_USER 등)
    private String authrtNm; // 권한명
}
