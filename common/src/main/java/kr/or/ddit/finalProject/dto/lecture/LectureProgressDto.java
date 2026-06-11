package kr.or.ddit.finalProject.dto.lecture;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LectureProgressDto {

    private String userId; // 사용자 ID (학생)
    private Long courseSn; // 강좌 고유 번호
    private Long lectureSn; // 강의 고유 번호
    private Integer secondsWatched; // 마지막에 저장된 시청 시간(초)
    private LocalDateTime lastUpdate;// 마지막 업데이트 시간
}
