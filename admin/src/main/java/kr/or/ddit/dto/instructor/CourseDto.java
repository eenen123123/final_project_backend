package kr.or.ddit.dto.instructor;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CourseDto {

    private Long courseSn;
    private Long curriculumId;
    private Long subjId;
    private Long subjClId;
    private String instrUserId;
    private String courseNm;
    private String courseExplnCn;
    private String thmbImg;
    private String totLrnTimeCnt;
    private String opnnYn;
    private String atchFileId;
    private String prodMthdCd;
    private Long coursePrice;
    private String rgtrId;
    private String lastMdfrId;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;

    // 조회 시 JOIN으로 가져오는 부가 정보
    private String curriculumTitle;
    private String subjNm;
    private String subjClNm;
    private int classCount;
}
