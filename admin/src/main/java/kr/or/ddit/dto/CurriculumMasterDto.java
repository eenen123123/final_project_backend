package kr.or.ddit.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = "curriculumId")
@Data
public class CurriculumMasterDto {

    @NotNull
    private Integer curriculumId;
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

    private CurriculumDetailDto curriculumDetail; // 마스터-상세 연계용 DTO 필드

}
