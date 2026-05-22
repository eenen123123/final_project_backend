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
public class QnaDto implements Serializable {

    private Long postSn; // 기본키(PK) · 시퀀스
    private String qnaCtgCd;
    private String secrYn; // Y:비공개 / N:공개
    private String answStatCd;
    private String answCn;
    private String answrUserId; // MEMBER.USER_ID 참조
    private LocalDateTime answDt;
}