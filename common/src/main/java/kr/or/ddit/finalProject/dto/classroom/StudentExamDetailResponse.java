package kr.or.ddit.finalProject.dto.classroom;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StudentExamDetailResponse {
    private Long examSn;
    private String examNm;
    private String examEndDt;
    private List<QuestionItem> questions;

    @Getter
    @AllArgsConstructor
    public static class QuestionItem {
        private Long qstnSn;
        private int qstnNo;
        private String qstnCn;
        private String qstnType;
        private java.math.BigDecimal score;
        private List<String> choices; // 객관식만 non-null
    }
}
