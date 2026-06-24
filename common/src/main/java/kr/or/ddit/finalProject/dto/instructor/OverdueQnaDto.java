package kr.or.ddit.finalProject.dto.instructor;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OverdueQnaDto {
    private Long postSn;
    private String postSj;
    private LocalDateTime regDt;
    private String instrUserId;
    private String instrUserNm;
    private String wrtrUserId;
    private String wrtrUserNm;
    private long elapsedHours;
}
