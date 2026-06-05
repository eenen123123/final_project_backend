package kr.or.ddit.finalProject.dto.classroom;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AchievementDto {
    private String label;       // "강의 수강", "과제 제출", "시험 응시"
    private int pct;            // 0 ~ 100
    private String color;       // SVG stroke 색상 (#hex)
    private double dashOffset;  // 125.66 * (1 - pct/100)
}
