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
public class DataRoomDto implements Serializable {

    private Long postSn; // 게시판 번호
    private String dataCtg; // 자료실 카테고리
    private Long expsOrd; // 게시글 정렬순서
    private String accsLmtCd;// 접근 제한코드 (전체공개 01, 회원전용 02)

    // BOARD 조인 필드
    private String postSj;
    private String postCn;
    private String wrtrUserId;
    private String atchFileId;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;

    // 공통코드 조인 필드
    private String dataCtgNm; // 자료실 카테고리명
    private String accsLmtNm; // 접근제한명
}
