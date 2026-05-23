package kr.or.ddit.finalProject.dto.exam;

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
public class QuestionListDto implements Serializable {

    private Long qstnSn; // 기본키(PK) · 시퀀스
    private String subjDtclCd; // 과목 소분류 COM_CD 참조
    private String rgtrId;
    private String lastMdfrId;
    private LocalDateTime regDt;
    private LocalDateTime field5;
    private String qstnTypeCd; // COM_CD 공통코드 참조
    private Integer allocScr; // 소수점 2자리
    private String corrAnswCn; // 정답 (객관식 번호 or 주관식 텍스트)
    private String atchFileId; // 공통첨부파일분류
    private String qstnCn; // JSON 형식: 보기·지문·이미지 포함
    private String explnCn;
    private String statCd; // 문제제출시에만 검사
}