package kr.or.ddit.finalProject.dto.coursecohort;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseCohortListResponse {

    private Long cohortSn;
    private String cohortYear;
    private String cohortStatCd;
    private String cohortStrtYmd;   // YYYYMMDD → 서비스에서 YYYY.MM.DD 포맷
    private String cohortEndYmd;    // YYYYMMDD → 서비스에서 YYYY.MM.DD 포맷
    private Integer curEnrlCnt;
    private String totLrnTimeCnt;

}
