package kr.or.ddit.finalProject.dto.learning;

import kr.or.ddit.finalProject.dto.classroom.ClassStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningOverviewDto {

    private Long classSn;
    private String classNm;
    private String courseNm;
    private Long courseSn;
    private ClassStatus classStatCd;
    private String enrlStrtYmd;
    private String enrlEndYmd;

    private int totalStudents;     // 총 수강생 (수강중 + 이수완료)
    private int activeStudents;    // 수강중 (ENRL_STAT_CD='01')
    private int completedStudents; // 이수완료 (ENRL_STAT_CD='02')
    private int totalLectures;     // 해당 강좌의 전체 강의 수

    private double completionRate;   // 이수율 (%) = 이수완료 / 총수강생
    private double avgProgressRate;  // 평균 진도율 (%) = 전체완료수 / (수강생수 × 전체콘텐츠수)
}
