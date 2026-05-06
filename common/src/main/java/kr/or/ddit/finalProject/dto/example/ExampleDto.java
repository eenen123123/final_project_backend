package kr.or.ddit.finalProject.dto.example;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 예시 DTO 클래스
 * 
 * DB에서 조회한 데이터를 담는 객체
 */
@Data
@AllArgsConstructor
public class ExampleDto {
    private LocalDate exampleDate;
}
