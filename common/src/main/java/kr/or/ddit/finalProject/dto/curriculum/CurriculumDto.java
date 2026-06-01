package kr.or.ddit.finalProject.dto.curriculum;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = "curriculumId")
@Data
public class CurriculumDto {

    @NotNull
    private Long curriculumId;
    @NotBlank
    private String title;
    @NotBlank
    private String instructorId;
    private String useYn;
    @NotBlank
    private String rgtrId;
    private LocalDate regDt;
    private String lastMdfrId;
    private LocalDate mdfcnDt;

}
