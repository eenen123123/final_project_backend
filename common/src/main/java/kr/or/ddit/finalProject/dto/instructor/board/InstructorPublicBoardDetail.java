package kr.or.ddit.finalProject.dto.instructor.board;

import java.util.List;

import lombok.Data;

@Data
public class InstructorPublicBoardDetail {
    private Long postSn;
    private String boardTypeCd;
    private String title;
    private String content;
    private String writerName;
    private String regDt;
    private Integer viewCount;
    private String hasFile;
    // QnA 답변 (boardTypeCd='03' 일 때만 유효)
    private String answerYn;
    private String answerContent;
    private String answererName;
    private String answerDt;
    // 이전/다음글 (같은 boardType 내)
    private PostNavItem prevPost;
    private PostNavItem nextPost;
    // 첨부파일 목록 (hasFile='Y' 일 때만 채워짐)
    private List<InstructorBoardFileItem> files;
}
