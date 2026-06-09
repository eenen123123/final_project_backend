package kr.or.ddit.finalProject.dto.instructor;

import lombok.Data;

@Data
public class InstructorPublicBoardItem {
    private Long postSn;
    private String title;
    private String regDt;
    private Integer viewCount;
    private String writerNickname; // QnA: 질문자 닉네임
    private String answerYn;       // QnA: 답변 여부
    private String hasFile;        // 자료실: 첨부파일 존재 여부 (Y/N)
}
