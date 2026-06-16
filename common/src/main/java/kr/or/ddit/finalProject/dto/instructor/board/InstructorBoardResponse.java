package kr.or.ddit.finalProject.dto.instructor.board;

import java.io.Serializable;
import java.util.List;

import kr.or.ddit.finalProject.dto.file.FileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 강사 게시판 응답 DTO, 강사 게시판 목록 조회 및 상세 조회에 사용
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorBoardResponse implements Serializable {

    private Long postSn; // 게시글 번호
    private String useYn; // 사용여부 (Y: 활성, N: 삭제)
    private String boardTypeCd; // 게시판 분류 코드
    private String boardTypeNm; // 게시판 분류명
    private String userName; // 작성자 이름
    private String title; // 게시글 제목
    private String content; // 게시글 내용
    private Long inqCnt; // 조회수
    private String regDt; // 게시글 등록일
    private String mdfcnDt; // 게시글 수정일
    private String atchFileId; // 첨부파일 ID

    private List<FileDto> files; // 첨부파일 목록 (없으면 null)

    // Q&A 답변 (boardTypeCd='QNA' 일 때만 채워짐, 아니면 null)
    private InstructorQnaAnswerDto answer;

}
