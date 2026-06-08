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
public class CourseListDto implements Serializable {

    // COURSE 테이블
    private Long courseSn; // 강좌 고유번호 (PK)
    private String courseNm; // 강좌명
    private String courseExplnCn; // 강좌 설명
    private String thmbImg; // 썸네일 이미지
    private Long coursePrice; // 수강료
    private String opnnYn; // 공개 여부
    private String prodMthdCd; // 판매 방식
    private String totLrnTimeCnt; // 총 학습시간 (예: 01:12:24)
    private LocalDateTime regDt; // 등록일 (NEW 뱃지 기준)

    // JOIN 전용 필드 (DB 컬럼 없음)
    private String instrUserNm; // 강사명 (MEMBER.USER_NAME)
    private String subjNm; // 소분류 과목명 (SUBJECT.SUBJ_NM)
    private Long subjClId; // 대분류 ID (필터 조건용)
    private String subjClNm; // 대분류 과목명 (SUBJECT_CLASSIFICATION.SUBJ_CL_NM)

    // 집계 필드
    private Integer lectureCnt; // 강의 수 (COUNT)
}
