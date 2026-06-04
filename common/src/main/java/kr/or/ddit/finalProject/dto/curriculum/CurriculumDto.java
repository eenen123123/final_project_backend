package kr.or.ddit.finalProject.dto.curriculum;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = "curriculumId")
@Data
public class CurriculumDto {

    private Long curriculumId;
    @NotBlank
    private String title;
    @NotBlank
    private String instructorId;
    private String useYn;
    @NotBlank
    private String rgtrId;
    private LocalDateTime regDt;
    private String lastMdfrId;
    private LocalDateTime mdfcnDt;

}
