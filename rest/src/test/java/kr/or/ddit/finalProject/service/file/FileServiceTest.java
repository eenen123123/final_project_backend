package kr.or.ddit.finalProject.service.file;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import kr.or.ddit.finalProject.dto.file.FileDto;
import kr.or.ddit.finalProject.mapper.FileMapper;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@SpringBootTest
public class FileServiceTest {

    @Autowired
    private FileMapper fileMapper;


    @Test
    public void getFileInfoTest() {
        long fileServerId = 184;
        FileDto fileDto = fileMapper.findContextByFileServerId(fileServerId);
        log.info("File Info: {}", fileDto);
    }
}
