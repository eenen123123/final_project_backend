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
    private int studentCount;         // 수강중(ENROLLED) 학생 수
    private String instrNm;           // 강사명
    private String enrlStrtYmd;       // 수강 신청 시작일
    private String enrlEndYmd;        // 수강 신청 종료일
    private LocalDateTime regDt;      // MyBatis 매핑용
    private String formattedRegDt;    // 서비스에서 포맷팅 후 세팅

}
