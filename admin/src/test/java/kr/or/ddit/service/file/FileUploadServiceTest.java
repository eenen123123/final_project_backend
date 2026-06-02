package kr.or.ddit.service.file;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import kr.or.ddit.finalProject.dto.file.FileCtxType;
import kr.or.ddit.finalProject.dto.member.MemberRoleEnum;
import kr.or.ddit.finalProject.service.file.FileUploadService;

@SpringBootTest
public class FileUploadServiceTest {

    @Autowired
    private FileUploadService fileUploadService;

    @Test
    void testUploadFile() throws IOException {
        ClassPathResource resource = new ClassPathResource("/static/200.gif");
        Path path = Paths.get(resource.getURI());

        try (FileInputStream fis = new FileInputStream(path.toFile())) {
            MultipartFile multipartFile =
                    new MockMultipartFile("file", "200.gif", "image/gif", fis);
            int atchFileId = 1; // 실제 테스트에서는 적절한 atchFileId로 변경
            var ctxType = FileCtxType.MEMBER_ROLE;
            var ctxId = MemberRoleEnum.ROLE_USER.name();
            var fileDto = fileUploadService.uploadFile(multipartFile, "testUser01", atchFileId,
                    ctxType, ctxId);
            System.out.println("Uploaded file info: " + fileDto);
        }
    }
}
