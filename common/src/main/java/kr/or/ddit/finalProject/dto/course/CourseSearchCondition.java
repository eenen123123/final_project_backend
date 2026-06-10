package kr.or.ddit.finalProject.dto.course;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseSearchCondition {
    private String instructorName;
    private String subjectName;
    private String courseName;
}
