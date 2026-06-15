package kr.or.ddit.finalProject.dto.subject;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectClassificationDto implements Serializable {

    private Long subjClId; // 기본키(PK) · 자동증가
    private String subjClNm;
    private String useYn; // Y : 여(예) N : 부(아니요)
    private String rgtrId;
    private String lastMdfrId;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;

    private List<SubjectDto> subjects; // 해당 분류에 속한 과목 목록
}
