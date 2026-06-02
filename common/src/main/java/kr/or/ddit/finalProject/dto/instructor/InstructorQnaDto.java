package kr.or.ddit.finalProject.dto.instructor;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorQnaDto implements Serializable {

    private Long postSn;
    private String qstnTypeCd; // COM_CD 참조
    private Long courseSn; // COURSE.COURSE_SN 참조
    private String answYn; // Y:답변완료/N:답변대기
    private String secrYn; // Y:비밀 / N:공개
}
