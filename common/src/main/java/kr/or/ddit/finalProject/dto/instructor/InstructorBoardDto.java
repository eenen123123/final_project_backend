package kr.or.ddit.finalProject.dto.instructor;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorBoardDto implements Serializable {

    private Long postSn; // 기본키(PK) · 시퀀스
    private String instrUserId;
    private String wrtrUserId; // MEMBER.USER_ID 참조

    @Size(max = 10)
    private String boardTypeCd; // COM_CD 공통코드 참조

    @NotBlank
    @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
    private String postSj;

    @NotBlank
    private String postCn;

    private Long inqCnt;
    private Long atchFileId; // 공통첨부파일분류 참조
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
    private String rgtrId;
    private String lastMdfrId;

    private String useYn; // 사용여부 (Y: 활성, N: 삭제)
    private Long classSn; // NULL=강사 페이지, 값 있으면 클래스룸 전속

    // 조인 컬럼
    private String boardTypeNm; // COM_CD.COM_CD_NM (게시판 분류명)
    private MemberDto memberDto; // 작성자 정보 포함
}
