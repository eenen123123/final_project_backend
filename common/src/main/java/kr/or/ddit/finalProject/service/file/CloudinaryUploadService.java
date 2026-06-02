package kr.or.ddit.finalProject.service.file;

import java.io.IOException;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.Cloudinary;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CloudinaryUploadService {
    private final Cloudinary cloudinary;


    /**
     * 파일을 Cloudinary에 업로드하고, 업로드된 파일의 URL을 반환하는 메서드입니다.
     * 
     * <pre>
     * 지원하는 파일 형식: 이미지, PDF
     * </pre>
     * 
     * <p>
     * 업로드 결과 예시: http://res.cloudinary.com/dczqmsune/image/upload/v1780362927/dwiyx1icfe4wxvxi9dpm.png
     * @param file 업로드할 파일
     * @return 업로드된 파일의 URL
     * @throws IOException 
     */
    public String uploadFileToCloudinary(MultipartFile file) throws IOException {
        // file 형식 체크
        String contentType = file.getContentType();
        if (contentType == null
                || !(contentType.startsWith("image/") || contentType.equals("application/pdf"))) {
            throw new FinalProjectException(ErrorCode.INVALID_FILE_TYPE_TO_CLOUDINARY);
        }

        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), Map.of());
        return (String) uploadResult.get("url");
    }
}
