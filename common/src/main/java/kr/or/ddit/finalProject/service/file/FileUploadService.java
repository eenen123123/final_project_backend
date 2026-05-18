package kr.or.ddit.finalProject.service.file;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import kr.or.ddit.finalProject.dto.file.StoredFileResponse;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import lombok.extern.slf4j.Slf4j;

// 파일 업로드 서비스 클래스

@Slf4j
@Service
public class FileUploadService {
    @Value("${file.server.upload-url:https://paste.maerchen.dev/api/storage/files}")
    private String fileServerPath;

    private final RestClient restClient = RestClient.create();

    /*  
        파일 서버에서 처리하는 컨트롤러는 아래와 같다
    @PostMapping
    public ResponseEntity<StoredFileResponse> upload(
            @RequestParam MultipartFile file,
            Authentication authentication) throws IOException {
        return ResponseEntity.ok(storedFileService.upload(file, uploadedBy(authentication)));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<StoredFileResponse> findById(@PathVariable long id) {
        return ResponseEntity.ok(storedFileService.findById(id));
    }
    
    @GetMapping("/{id}/view")
    public ResponseEntity<?> view(
            @PathVariable long id,
            @RequestHeader HttpHeaders headers) throws IOException {
        StoredFile file = storedFileService.findEntityById(id);
        Resource resource = storedFileService.loadResource(file);
        MediaType mediaType = MediaType.parseMediaType(file.getContentType());
    
        if (!headers.getRange().isEmpty()) {
            ResourceRegion region = resourceRegion(resource, headers.getRange().getFirst());
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(mediaType)
                    .body(region);
        }
    
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(resource.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION, inlineDisposition(file.getOriginalFilename()))
                .body(resource);
    }
    
    
    */


    // 파일 업로드 메서드
    public StoredFileResponse uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FinalProjectException(ErrorCode.FILE_EMPTY);
        }

        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", multipartResource(file));

            return restClient.post().uri(fileServerPath).contentType(MediaType.MULTIPART_FORM_DATA)
                    .headers(this::relayAuthorizationHeader).body(body).retrieve()
                    .body(StoredFileResponse.class);
        } catch (RestClientResponseException e) {
            log.error("파일 서버 업로드 실패. status={}, body={}", e.getStatusCode(),
                    e.getResponseBodyAsString(), e);
            throw new FinalProjectException(ErrorCode.FILE_UPLOAD_FAILED);
        } catch (IOException e) {
            log.error("파일 처리 중 오류 발생", e);
            throw new FinalProjectException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private ByteArrayResource multipartResource(MultipartFile file) throws IOException {
        return new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
    }

    private void relayAuthorizationHeader(HttpHeaders headers) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && !authorization.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }
    }

}
