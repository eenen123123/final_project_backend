package kr.or.ddit.finalProject.dto.lecture;

import lombok.Data;

@Data
public class LectureDto {

    private Long lectureSn;
    private Long courseSn;
    private String lectureNm;
    private String lectureTypeCd;
    private String atchFileId; // 영상 파일 아이디
    private Integer lectureDuration;
    private String lectureExplnCn;
    private String opnnYn;
    private String lockYn;
    private Long prereqLectureSn;
    private Integer sortOrd;
    private String rgtrId;
    private String lastMdfrId;

}
