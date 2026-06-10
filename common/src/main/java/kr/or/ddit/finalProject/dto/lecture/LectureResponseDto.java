package kr.or.ddit.finalProject.dto.lecture;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LectureResponseDto implements Serializable {
    private Long lectureSn;
    private Long courseSn;
    private String lectureName;
    private Long lectureVideoFileId; // 영상 파일 아이디
    private Integer lectureDuration; // 강의 시간 (초 단위)
    private String lectureExplanation; // 강의 설명
}
