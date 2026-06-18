package kr.or.ddit.finalProject.dto.classroom;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomMemberListResponse {

    private String userId;
    private String userName;
    private EnrollStatus enrlStatCd;
    private LocalDateTime regDt;    // MyBatis 매핑용
    private String formattedRegDt;  // 서비스에서 포맷팅 후 세팅

}
