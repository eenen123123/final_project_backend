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
import kr.or.ddit.finalProject.service.file.CloudinaryUploadService;

@SpringBootTest
public class CloudinaryUploadServiceTest {

    @Autowired
    private CloudinaryUploadService cloudinaryUploadService;

    @Test
    void testUploadFileToCloudinary() throws IOException {
        ClassPathResource resource = new ClassPathResource("/static/favicon.ico");
        Path path = Paths.get(resource.getURI());

        try (FileInputStream fis = new FileInputStream(path.toFile())) {
            MultipartFile multipartFile =
                    new MockMultipartFile("file", "favicon.ico", "image/x-icon", fis);
            String url = cloudinaryUploadService.uploadFileToCloudinary(multipartFile);
            System.out.println("Uploaded file URL: " + url);
        }


    }
}
