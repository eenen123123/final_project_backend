package kr.or.ddit.finalProject.dto.parent;

import lombok.Data;

@Data
public class ParentChildDto {

    private String studentId;
    private String studentName;
    private String enrlSchlNm;

    private Long classSn;
    private String classroomName;
    private String instructorName;
    private String enrollStartDate;

    private Integer assignmentRate;
    private Double recentExamAvgScore;
}
