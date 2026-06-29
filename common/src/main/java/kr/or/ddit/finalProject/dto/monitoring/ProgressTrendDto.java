package kr.or.ddit.finalProject.dto.monitoring;

import lombok.Data;

@Data
public class ProgressTrendDto {
    private String mon;           // YYYY-MM
    private String classNm;
    private double progressRate;  // 0~100
}
