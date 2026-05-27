package kr.or.ddit.finalProject.dto.exam;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestionDto implements Serializable {

    private Long examSn;
    private Long qstnSn;
    private Long qstnOrdr;
}
