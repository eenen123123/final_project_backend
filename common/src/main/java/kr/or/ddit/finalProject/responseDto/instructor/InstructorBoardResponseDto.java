package kr.or.ddit.finalProject.responseDto.instructor;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 강사 게시판 응답 DTO, 강사 게시판 목록 조회 및 상세 조회에 사용
 */
@Data
@NoArgsConstructor
public class InstructorBoardResponseDto implements Serializable {

    private int postSn; // 게시글 번호
    private String userName; // 작성자 이름
    private String title; // 게시글 제목
    private String content; // 게시글 내용
    private String regDt; // 게시글 등록일
    private String mdfcnDt; // 게시글 수정일
    private String atchFileId; // 첨부파일 ID

}
