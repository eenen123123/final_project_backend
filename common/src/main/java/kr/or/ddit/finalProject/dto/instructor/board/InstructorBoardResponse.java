package kr.or.ddit.finalProject.dto.instructor.board;

import java.io.Serializable;
import java.util.List;

import kr.or.ddit.finalProject.dto.file.FileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 강사 게시판 응답 DTO.
 * 목록과 상세 조회 결과를 뷰로 전달할 때 사용한다.
 * InstructorBoardDto(엔티티 DTO)를 서비스 레이어에서 변환하여 생성한다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorBoardResponse implements Serializable {

    /** 게시글 일련번호 (PK) */
    private Long postSn;

    /** 화면 표시 번호 (totalCount - offset - index, 역순 채번) */
    private int displayNo;

    /** 사용여부 (Y: 활성, N: 소프트 삭제됨) */
    private String useYn;

    /** 게시판 분류 코드 (NOTICE / QNA / DATAROOM) */
    private String boardTypeCd;

    /** 게시판 분류명 (BoardType enum에서 변환한 한글 레이블) */
    private String boardTypeNm;

    /** 작성자 이름 */
    private String userName;

    /** 게시글 제목 */
    private String title;

    /** 게시글 본문 HTML (목록에서는 null, 상세에서만 채워짐) */
    private String content;

    /** 조회수 */
    private Long inqCnt;

    /** 등록일시 문자열 (목록: yyyy-MM-dd, 상세: yyyy-MM-dd HH:mm:ss) */
    private String regDt;

    /** 최종 수정일시 문자열 */
    private String mdfcnDt;

    /** 첨부파일 그룹 ID 문자열 (없으면 null) */
    private String atchFileId;

    /** 첨부파일 목록 (상세 조회 시 atchFileId가 있을 때만 채워짐) */
    private List<FileDto> files;

    /** Q&A 답변 정보 (boardTypeCd == 'QNA' 일 때만 채워짐, 그 외 null) */
    private InstructorQnaAnswerDto answer;
}
