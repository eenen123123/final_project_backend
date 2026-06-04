package kr.or.ddit.finalProject.dto.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FaqDto extends BoardDto {

    private String faqCtgCd;    // 대분류 (CL_CODE: 101)
    private String faqSubCtgCd; // 중분류 (CL_CODE: 102)
    private Long expsOrd;       // 노출 순서
    private String topFixYn;    // Y:고정(BEST) / N:미고정

    // 공통코드 조인 필드
    private String faqCtgNm;
    private String faqSubCtgNm;
}
