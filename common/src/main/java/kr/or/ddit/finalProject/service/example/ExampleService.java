package kr.or.ddit.finalProject.service.example;

import org.springframework.stereotype.Service;

@Service
public interface ExampleService {
    /**
     * 예시 메서드
     * DB에서 날짜를 조회하여 문자열로 반환하는 예시 메서드
     * 
     * @return 조회된 날짜 문자열
     */
    public String getExampleDate();
}
