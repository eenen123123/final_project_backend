package kr.or.ddit.finalProject.dto.exam;

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
public class QuestionPassageDto implements Serializable {

    private Long passSn; // 시퀀스
    private Long subjId; // SUBJ 테이블 참조
    private String passCn;
    private String statCd; // 문제생성시에만 검사
    private String rgtrId;
    private String lastMdfrId;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
}
