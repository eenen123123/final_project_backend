package kr.or.ddit.finalProject.dto.student;

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
public class StudentBlackListDto implements Serializable {

    private String stdUserId;
    private LocalDateTime blklstStrtDt;
    private LocalDateTime blklstEndDt; // NULL = 영구 블랙리스트
}