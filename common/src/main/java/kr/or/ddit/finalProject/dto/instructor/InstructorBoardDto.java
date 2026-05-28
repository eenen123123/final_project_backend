package kr.or.ddit.finalProject.dto.instructor;

import java.io.Serializable;
import java.time.LocalDateTime;

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
    private String boardTypeCd; // COM_CD 공통코드 참조
    private String postSj;
    private String postCn;
    private Long inqCnt;
    private Long atchFileId; // 공통첨부파일분류 참조
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
    private String rgtrId;
    private String lastMdfrId;

    private MemberDto memberDto; // 작성자 정보 포함
}
