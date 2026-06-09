package kr.or.ddit.finalProject.dto.instructor;

import lombok.Data;

@Data
public class InstructorPublicBoardDetail {
    private Long postSn;
    private String boardTypeCd;
    private String title;
    private String content;
    private String writerNickname;
    private String regDt;
    private Integer viewCount;
    private String hasFile;
    // QnA 답변 (boardTypeCd='03' 일 때만 유효)
    private String answerYn;
    private String answerContent;
    private String answererName;
    private String answerDt;
}
