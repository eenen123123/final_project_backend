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
    private String enrlStatCd;      // 01=수강중, 02=수강완료, 03=중도탈퇴, 04=등록취소
    private LocalDateTime regDt;    // MyBatis 매핑용
    private String formattedRegDt;  // 서비스에서 포맷팅 후 세팅

}
