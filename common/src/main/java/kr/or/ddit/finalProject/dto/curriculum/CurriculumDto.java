package kr.or.ddit.finalProject.dto.curriculum;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = "curriculumId")
@Data
public class CurriculumDto {

    private Long curriculumId;
    private String title;
    private String instructorId;
    private String useYn;
    private LocalDate strtDt;
    private LocalDate endDt;
    private String explnCn;
    private String rgtrId;
    private LocalDateTime regDt;
    private String lastMdfrId;
    private LocalDateTime mdfcnDt;

}
