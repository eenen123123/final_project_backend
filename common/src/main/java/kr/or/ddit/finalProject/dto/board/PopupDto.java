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
public class PopupDto implements Serializable {

    private Long popupSn; // 기본키(PK) · 시퀀스
    private String field;
    private LocalDateTime popupStrtDt;
    private LocalDateTime popupEndDt;
    private String popupExpsYn; // Y:노출 / N:미노출
    private String delYn; // Y:삭제 / N:정상
    private String rgtrId;
    private String lastMdfrId;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
}