package kr.or.ddit.finalProject.dto.classroom;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TodayQuestionDto {
    private String subject;  // 강좌명 (부제)
    private String content;  // 시험명 (본문)
}
