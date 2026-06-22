package kr.or.ddit.finalProject.dto.lecture;

import lombok.Data;

/**
 * 특정 수강생의 강의별 진도 상세 응답 DTO.
 * 공개 강의 단위로 완료 여부와 완료일시를 담는다.
 */
@Data
public class LectureProgressDetailResponse {

    private Long lectureSn;     // 강의 고유 번호
    private String lectureNm;   // 강의명
    private Integer sortOrd;    // 강의 정렬 순서 — SQL ORDER BY 결과 순서 보존용, 뷰에서 직접 사용하지 않음
    private String cmplYn;      // 해당 수강생의 강의 완료 여부 (Y: 완료, N: 미완료)
    private String cmplDt;      // 강의 완료일시 (yyyy-MM-dd HH:mm) — 미완료 시 null
}
