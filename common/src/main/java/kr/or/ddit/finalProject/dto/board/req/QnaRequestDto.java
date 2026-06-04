package kr.or.ddit.finalProject.dto.board.req;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class QnaRequestDto extends EditorPostRequestDto {

    private String qnaCtgCd; // QnA 카테고리 (CL_CODE: 105)
    private String secrYn; // 비공개 여부

}
