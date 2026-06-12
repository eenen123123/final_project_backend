package kr.or.ddit.finalProject.dto.curriculum;

import java.time.LocalDate;

import lombok.Data;

@Data
public class CurriculumSaveRequest {
    private String title;
    private LocalDate strtDt;
    private LocalDate endDt;
    private String explnCn;
}
