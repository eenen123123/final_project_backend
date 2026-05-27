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
public class InstructorDataRoomDto implements Serializable {

    private Long postSn;
    private Long dataCourseSn; // COURSE.COURSE_SN 참조
}
