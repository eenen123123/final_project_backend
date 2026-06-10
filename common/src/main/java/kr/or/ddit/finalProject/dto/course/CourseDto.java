package kr.or.ddit.finalProject.dto.course;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseDto {

    // ── COURSE 테이블 컬럼 ────────────────────────────────────────────────────
    private Long courseSn;
    private Long curriculumId;
    private Long subjId;
    private String instrUserId;
    private String courseNm;
    private String courseExplnCn;
    private String thmbImg;
    private String totLrnTimeCnt;
    private String opnnYn;
    private Long coursePrice;
    private Integer sortOrd;
    private String rgtrId;
    private String lastMdfrId;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;

    // ── JOIN / 집계 (DB 컬럼 아님) ────────────────────────────────────────────
    private String instrNm;          // MEMBER.USER_NAME AS INSTR_NM
    private String curriculumTitle;  // CURRICULUM.TITLE AS CURRICULUM_TITLE
    private String subjNm;           // SUBJECT.SUBJ_NM
    private Long subjClId;           // SUBJECT_CLASSIFICATION.SUBJ_CL_ID
    private String subjClNm;         // SUBJECT_CLASSIFICATION.SUBJ_CL_NM
    private Integer lectureCnt;      // COUNT(*) AS LECTURE_CNT
}
