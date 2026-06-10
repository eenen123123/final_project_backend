package kr.or.ddit.finalProject.dto.textbook;

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
public class TextbookDto implements Serializable {

    private Long textbookSn; // 기본키(PK) · 시퀀스
    private String textbookNm; // 교재명
    private String pubrNm; // 출판사명
    private String authrNm; // 저자명
    private String isbnNo; // 국제표준도서번호 (ISBN-13 기준)
    private String trgtGrdCn; // 예: 중학교 2학년 대상
    private Long subjId; // 소분류 ID (SUBJECT.SUBJ_ID 참조)
    private Long salePrcAmt; // 소비자 판매가 (원)
    private Long purchPrcAmt; // 교재 매입 원가 (원)
    private Long dlvrAmt; // 배송비 (원)
    private String thmbImg; // 교재 표지 이미지 URL (Cloudinary)
    private String useYn; // 논리 삭제 · DEFAULT 'Y'
    private String rgtrId;
    private String lastMdfrId;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;

    private String tagline; // 제목 위 태그라인
    private String bookSmry; // 2~3줄 간략 요약
    private String tocCn; // 목차
    private Long courseSn; // 연결 강좌 ID (COURSE.COURSE_SN)

    // JOIN 전용 필드 (DB 컬럼 없음)
    private String subjNm; // 과목명 (SUBJECT.SUBJ_NM)
    private String subjClNm; // 과목 대분류명 (SUBJECT_CLASSIFICATION.SUBJ_CL_NM)
    private Long subjClId; // 과목 대분류 ID (SUBJECT 통해 JOIN)
    private String instrUserNm; // 강사명 (COURSE → MEMBER)
    private String courseNm; // 강좌명 (COURSE.COURSE_NM)

    // 검색 조건 (DB 컬럼 없음)
    private String keyword;
    private boolean showArchived;
    private String sort; // recent(default) | price | stock
}
