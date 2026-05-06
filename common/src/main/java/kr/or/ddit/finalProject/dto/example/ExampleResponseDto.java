package kr.or.ddit.finalProject.dto.example;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

/**
 * 예시 Response DTO 클래스
 * 
 * 클라이언트에게 응답할 때 필요한 데이터를 담는 객체
 */
@Data
@Builder
public class ExampleResponseDto implements Serializable {
    private String exampleField;
}
