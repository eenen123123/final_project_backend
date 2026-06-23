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
public class ClassroomListResponse {

    private Long classSn;
    private String classNm;
    private String courseNm;
    private ClassStatus classStatCd;
    private int studentCount;         // 수강중(01) 학생 수
    private LocalDateTime regDt;      // MyBatis 매핑용
    private String formattedRegDt;    // 서비스에서 포맷팅 후 세팅

}
