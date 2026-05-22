package kr.or.ddit.finalProject.dto.board;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaqDto implements Serializable {

    private Long postSn; // 기본키(PK) · 시퀀스
    private String faqCtgCd; // COM_CD 공통코드 참조
    private Long expsOrd;
    private String topFixYn; // Y:고정 / N:미고정
}