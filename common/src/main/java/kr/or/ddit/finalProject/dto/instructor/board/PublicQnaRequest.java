package kr.or.ddit.finalProject.dto.instructor.board;

import lombok.Data;

@Data
public class PublicQnaRequest {
    private String title;
    private String content;
    private String secrYn;
}
