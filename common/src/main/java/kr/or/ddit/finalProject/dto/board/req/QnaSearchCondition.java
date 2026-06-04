package kr.or.ddit.finalProject.dto.board.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QnaSearchCondition {
    private String keyword;
    private String qnaCtgCd;
    private String answStatCd;
    private String wrtrUserId; // myOnly 필터용
}
