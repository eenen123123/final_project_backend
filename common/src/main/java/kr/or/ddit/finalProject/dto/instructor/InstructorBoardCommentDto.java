package kr.or.ddit.finalProject.dto.instructor;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorBoardCommentDto implements Serializable {

    private Long cmntSn; // 기본키(PK) · 시퀀스
    private Long postSn;
    private String wrtrUserId; // MEMBER.USER_ID 참조
    private String cmntCn;
    private Long prntCmntSn; // Self-FK · NULL = 최상위 댓글 / 대댓글 구현
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
    private String rgtrId;
    private String lastMdfrId;
}
