package kr.or.ddit.finalProject.dto.board;

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
public class BoardCommentDto implements Serializable {

    private Long cmntSn; // 기본키(PK) · 시퀀스
    private Long postSn; // BOARD.POST_SN 참조
    private String wrtrUserId; // MEMBER.USER_ID 참조
    private Long prntCmntSn; // BOARD_COMMENT.CMNT_SN 자기참조
    private String cmntCn;
    private String delYn; // 삭제 시 내용 마스킹 처리 권장
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
}
