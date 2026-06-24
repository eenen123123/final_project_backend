package kr.or.ddit.finalProject.dto.course;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 강좌 정보 DTO. COURSE 테이블과 매핑되며 커리큘럼 배정, 강좌 목록 조회, 상세 조회에 사용된다. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseDto {

    // ── COURSE 테이블 컬럼 ────────────────────────────────────────────────────
    /** PK */
    private Long courseSn;
    /** 소속 커리큘럼 ID (FK → CURRICULUM.CURRICULUM_ID). 미배정 시 null */
    private Long curriculumId;
    /** 과목 ID (FK → SUBJECT.SUBJ_ID) */
    private Long subjId;
    /** 담당 강사 ID (FK → MEMBER.USER_ID) */
    private String instrUserId;
    /** 강좌명 */
    private String courseNm;
    /** 강좌 설명 */
    private String courseExplnCn;
    /** 썸네일 이미지 경로 */
    private String thmbImg;
    /** 총 학습 시간 (HH:MM:SS 형식) */
    private String totLrnTimeCnt;
    /** 공개 여부. 'Y' = 공개, 'N' = 비공개 */
    private String opnnYn;
    /** 수강료 */
    private Long coursePrice;
    /** 커리큘럼 내 정렬 순서. 미배정 시 null */
    private Integer sortOrd;
    /** 최초 등록자 ID */
    private String rgtrId;
    /** 최종 수정자 ID */
    private String lastMdfrId;
    /** 최초 등록일시 */
    private LocalDateTime regDt;
    /** 최종 수정일시 */
    private LocalDateTime mdfcnDt;

    // ── JOIN / 집계 (DB 컬럼 아님) ────────────────────────────────────────────
    /** 강사명. MEMBER.USER_NAME */
    private String instrNm;
    /** 소속 커리큘럼명. CURRICULUM.TITLE */
    private String curriculumTitle;
    /** 과목명. SUBJECT.SUBJ_NM */
    private String subjNm;
    /** 과목 분류 ID. SUBJECT_CLASSIFICATION.SUBJ_CL_ID */
    private Long subjClId;
    /** 과목 분류명. SUBJECT_CLASSIFICATION.SUBJ_CL_NM */
    private String subjClNm;
    /** 강의 수. COUNT(*) */
    private Integer lectureCnt;
}
