package kr.or.ddit.finalProject.dto.course;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponseDto implements Serializable {
    private Long courseSn; // 기본키(PK) · 시퀀스
    private String courseName;
    private String instructorName;
    private String instructorProfileImg;
    private Long subjectId;
    private String subjectName;
    private Long coursePrice;
    private String explain;
    private boolean isNew; // NEW 뱃지 표시 여부
    private boolean isBest; // BEST 뱃지 표시 여부
}
