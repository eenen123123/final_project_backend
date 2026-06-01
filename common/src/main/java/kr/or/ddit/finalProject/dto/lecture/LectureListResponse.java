package kr.or.ddit.finalProject.dto.lecture;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureListResponse {

    private Long lectSn;
    private String lectYear;
    private String lectStatCd;
    private String lectStrtYmd;     // YYYYMMDD → 서비스에서 YYYY.MM.DD 포맷
    private String lectEndYmd;      // YYYYMMDD → 서비스에서 YYYY.MM.DD 포맷
    private Integer curEnrlCnt;
    private String lrnTimeCnt;

}
