package kr.or.ddit.finalProject.dto.curriculum;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = "curriculumId")
@Data
public class CurriculumDto {

    private Long curriculumId;
    private String title;
    private String instructorId;
    private String useYn;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate strtDt;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDt;
    private String explnCn;
    private String rgtrId;
    private LocalDateTime regDt;
    private String lastMdfrId;
    private LocalDateTime mdfcnDt;
    /** 커리큘럼에 배정된 강좌 수 (selectList 조회 시 서브쿼리로 채워짐) */
    private int courseCount;

}
