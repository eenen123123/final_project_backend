package kr.or.ddit.finalProject.dto.board;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaqDto implements Serializable {

    private Long postSn; // FK → BOARD.POST_SN
    private String faqCtgCd; // 대분류 (CL_CODE: 101)
    private String faqSubCtgCd; // 중분류 (CL_CODE: 102) ← 추가
    private Long expsOrd; // 노출 순서
    private String topFixYn; // Y:고정(BEST) / N:미고정

    // BOARD 테이블 조인 필드
    private String postSj; // 질문 제목
    private String postCn; // 답변 내용
    private String wrtrUserId; // 작성자
    private java.time.LocalDateTime regDt; // 등록일

    // 공통코드 조인 필드 (화면 표시용)
    private String faqCtgNm; // 대분류명
    private String faqSubCtgNm; // 중분류명
}
