package kr.or.ddit.finalProject.dto.monitoring;

import lombok.Data;

@Data
public class InstructorMonitorCardDto {
    private String userId;
    private String userName;
    private String jbgrCd;
    private int thisMonthCnt;
}
