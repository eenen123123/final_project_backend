package kr.or.ddit.finalProject.dto.textbook;

import java.io.Serializable;
import java.time.LocalDate;
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
    private String textbookNm;
    private String pubrNm;
    private String authrNm;
    private String isbnNo; // 국제표준도서번호 (ISBN-13 기준)
    private String trgtGrdCn; // 예: 중학교 2학년 대상
    private String subjCd; // SUBJ.SUBJ_CD 참조
    private Long salePrcAmt; // 소비자 판매가 (원)
    private Long purchPrcAmt; // 교재 매입 원가 (원)
    private String useYn; // 논리 삭제 · DEFAULT 'Y'
    private String rgtrId;
    private String lastMdfrId;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
}