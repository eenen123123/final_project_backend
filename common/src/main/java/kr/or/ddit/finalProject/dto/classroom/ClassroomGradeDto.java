package kr.or.ddit.finalProject.dto.classroom;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClassroomGradeDto {

    private String userId;
    private String userName;

    private int totAsgmtCnt;
    private int sbmtAsgmtCnt;
    private int grddAsgmtCnt;
    private BigDecimal avgScore;

    private int examCnt;
}
