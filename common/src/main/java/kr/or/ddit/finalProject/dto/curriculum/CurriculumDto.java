package kr.or.ddit.finalProject.dto.curriculum;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = "curriculumId")
@Data
public class CurriculumDto {

    private Long curriculumId;
    @NotBlank(message = "커리큘럼명은 필수 입력 항목입니다.")
    @Size(max = 200, message = "커리큘럼명은 200자 이내로 입력해 주세요.")
    private String title;
    private String instructorId;
    private String useYn;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate strtDt;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDt;
    @Size(max = 4000, message = "설명은 4000자 이내로 입력해 주세요.")
    private String explnCn;
    private String rgtrId;
    private LocalDateTime regDt;
    private String lastMdfrId;
    private LocalDateTime mdfcnDt;
    /** 커리큘럼에 배정된 강좌 수 (selectList 조회 시 서브쿼리로 채워짐) */
    private int courseCount;

}
