package kr.or.ddit.finalProject.dto.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSearchCondition {
    private String keyword;
    private String instrNm;
    private String opnnYn;
    private Long curriculumId;
    private Long subjClId;
    private Long subjId;
}
