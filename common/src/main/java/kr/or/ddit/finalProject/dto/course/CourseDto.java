package kr.or.ddit.finalProject.dto.course;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto implements Serializable {

    private Long courseSn; // 기본키(PK) · 시퀀스
    private Long curriculumId; // 커리큘럼 번호(FK)
    private Long subjId;
    private String instrUserId;
    private String courseNm;
    private String courseExplnCn; // 해당 강좌 설명 및 소개
    private String thmbImg;
    private String totLrnTimeCnt; // 01:12:24
    private String opnnYn; // 강좌 공개 여부
    private String atchFileId; // 공통첨부파일분류
    private String prodMthdCd; // COM_CD 공통코드 참조
    private Long coursePrice; // 강좌 가격
    private String rgtrId;
    private String lastMdfrId;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
    private Long subjClId;
    private Integer sortOrd;
    private Long prereqCourseSn;

}
