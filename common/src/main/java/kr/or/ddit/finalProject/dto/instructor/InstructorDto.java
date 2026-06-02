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
public class InstructorDto implements Serializable {

    private String instrUserId; // 기본키(PK)
    private String instrIntro; // 학력/약력
}
