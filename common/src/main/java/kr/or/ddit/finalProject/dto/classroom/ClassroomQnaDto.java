package kr.or.ddit.finalProject.dto.classroom;

import java.time.LocalDateTime;

import kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClassroomQnaDto extends InstructorBoardDto {

    // INSTRUCTOR_QNA 전용 필드
    private String answYn;
    private String secrYn;
    private String answCn;
    private String answrUserId;
    private String answrUserNm;
    private LocalDateTime answDt;
}
