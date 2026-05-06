package kr.or.ddit.finalProject.dto.example;

import java.io.Serializable;
import lombok.Data;

/**
 * 예시 Request DTO 클래스
 * 
 * 클라이언트에서 요청할 때 필요한 데이터를 담는 객체
 */
@Data
public class ExampleRequestDto implements Serializable {
    private String exampleField;
}
