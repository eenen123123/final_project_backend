package kr.or.ddit.finalProject.dto.instructor.board;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * 강사 공개 게시판 상세 DTO (React 프론트 전용).
 * selectPublicBoardDetail 쿼리 결과를 담으며,
 * 서비스 레이어에서 이전/다음글과 첨부파일 목록을 추가로 채운다.
 */
@Data
public class InstructorPublicBoardDetail {

    /** 게시글 일련번호 (PK) */
    private Long postSn;

    /** 게시판 분류 코드 (NOTICE / QNA / DATAROOM) */
    private String boardTypeCd;

    /** 게시글 제목 */
    private String title;

    /** 게시글 본문 HTML */
    private String content;

    /** 작성자 이름 */
    private String writerName;

    /** 등록일시 (yyyy-MM-dd HH:mm:ss) */
    private String regDt;

    /** 조회수 */
    private Integer viewCount;

    /** 첨부파일 존재 여부 (Y/N) — Y일 때만 files 필드가 채워짐 */
    private String hasFile;

    /** Q&A 답변 완료 여부 (Y/N, boardTypeCd == 'QNA' 일 때만 유효) */
    private String answerYn;

    /** Q&A 답변 본문 */
    private String answerContent;

    /** Q&A 답변 작성자 이름 */
    private String answererName;

    /** Q&A 답변 등록일시 (yyyy-MM-dd HH:mm) */
    private String answerDt;

    /** 같은 boardTypeCd 내 이전 게시글 (없으면 null) */
    private PostNavItem prevPost;

    /** 같은 boardTypeCd 내 다음 게시글 (없으면 null) */
    private PostNavItem nextPost;

    /** 첨부파일 목록 (hasFile == 'Y' 일 때만 채워짐) */
    private List<InstructorBoardFileItem> files;

    /** 비밀글 여부 (Y/N, boardTypeCd == 'QNA' 일 때만 유효) */
    private String secrYn;

    /** 현재 로그인한 유저가 작성자인지 여부 */
    private boolean isMyPost;

    /** 작성자 ID — isMyPost 계산에만 사용, JSON 응답에서 제외 */
    @JsonIgnore
    private String wrtrUserId;
}
