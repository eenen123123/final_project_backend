package kr.or.ddit.finalProject.mapper.suneung;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class SuneungGradeCutMapperTest {
    @Autowired
    private SuneungGradeCutMapper suneungGradeCutMapper;

    @Test
    void selectSuneungGradeCutList() {
        var gradeCutList = suneungGradeCutMapper.selectSuneungGradeCutListByYear(2026);
        log.info("gradeCutList: {}", gradeCutList);
    }

    @Test
    void selectSubjectList() {
        var subjectList = suneungGradeCutMapper.selectSubjectList();
        log.info("subjectList: {}", subjectList);
    }
}
