package kr.or.ddit.finalProject.service.example;

import org.springframework.stereotype.Service;
import kr.or.ddit.finalProject.mapper.TestMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExampleServiceImpl implements ExampleService {
    private final TestMapper testMapper;

    @Override
    public String getExampleDate() {
        // TestMapper를 사용하여 DB에서 날짜를 조회
        return testMapper.getDate().getExampleDate().toString();
    }
}
