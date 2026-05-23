package kr.or.ddit.finalProject.dto.exam;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamTakerDto implements Serializable {

    private String stdUserId;
    private Long examSn;
}