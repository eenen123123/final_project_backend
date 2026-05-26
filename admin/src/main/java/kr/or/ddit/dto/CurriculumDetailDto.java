package kr.or.ddit.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = "detailId")
@Data
public class CurriculumDetailDto {

    @NotNull
    private Integer detailId;
    @NotNull
    private Integer curriculumId;
    @NotNull
    private Integer rowOrder;
    private String weekInfo;
    private String topic;
    private String content;
    @NotBlank
    private String rgtrId;
    private LocalDate regDt;
    private String lastMdfrId;
    private LocalDate mdfcnDt;
}
