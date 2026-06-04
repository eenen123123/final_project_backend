package kr.or.ddit.finalProject.dto.board.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoticeSearchCondition {
    private String keyword;
    private String noticeTypeCd;
}
