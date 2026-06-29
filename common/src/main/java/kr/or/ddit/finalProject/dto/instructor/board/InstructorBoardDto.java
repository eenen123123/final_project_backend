package kr.or.ddit.finalProject.dto.instructor.board;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kr.or.ddit.finalProject.dto.file.FileDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 강사 게시판 DTO.
 * INSERT/UPDATE 입력값과 SELECT 조회 결과를 공통으로 담는다.
 * CLASS_SN이 NULL이면 강사 홈페이지 게시판, 값이 있으면 특정 클래스룸 전속 게시글이다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorBoardDto implements Serializable {

    /** 게시글 일련번호 (PK, Oracle IDENTITY 컬럼 — INSERT 후 자동 채워짐) */
    private Long postSn;

    /** 게시판 소유 강사 ID (INSTRUCTOR.INSTR_USER_ID 참조) */
    private String instrUserId;

    /** 실제 작성자 ID (MEMBER.USER_ID 참조 — 학생이 쓴 Q&A는 학생 ID) */
    private String wrtrUserId;

    /** 게시판 분류 코드 (NOTICE / QNA / DATAROOM) */
    @NotBlank(message = "게시판 분류를 선택해주세요.")
    private String boardTypeCd;

    /** 게시글 제목 */
    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
    private String postSj;

    /** 게시글 본문 (Oracle CLOB) */
    @NotBlank(message = "내용을 입력해주세요.")
    private String postCn;

    /** 조회수 */
    private Long inqCnt;

    /** 첨부파일 그룹 ID (CMMT_ATCH_FILE_DTL.ATCH_FILE_ID 참조, 없으면 NULL) */
    private Long atchFileId;

    /** 등록일시 */
    private LocalDateTime regDt;

    /** 최종 수정일시 */
    private LocalDateTime mdfcnDt;

    /** 최초 등록자 ID */
    private String rgtrId;

    /** 최종 수정자 ID */
    private String lastMdfrId;

    /** 사용여부 (Y: 활성, N: 소프트 삭제) */
    private String useYn;

    /** 클래스룸 일련번호 (NULL이면 강사 홈페이지 게시판, 값 있으면 특정 클래스룸 전속) */
    private Long classSn;

    /** 작성자 이름 — MEMBER 조인 결과, SELECT 전용 */
    private MemberDto memberDto;

    /** Q&A 답변 여부 (Y/N) — INSTRUCTOR_QNA 조인 결과, 목록 조회 전용 */
    private String answYn;

    /** 첨부파일 목록 — 상세 조회 전용, atchFileId 기준으로 서비스 레이어에서 채워준다 */
    private List<FileDto> attachedFiles;
}
