package kr.or.ddit.finalProject.service.file;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import kr.or.ddit.finalProject.dto.file.FileDto;
import kr.or.ddit.finalProject.dto.file.StoredFileResponse;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.FileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 파일 업로드 서비스 클래스

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {
    @Value("${file.server.upload-url:https://paste.maerchen.dev/api/storage/files}")
    private String fileServerPath;

    private final RestClient restClient = RestClient.create();
    private final FileMapper fileUploadMapper;

    /*  
        파일 서버에서 처리하는 컨트롤러는 아래와 같다
    
        
     // 공통 접근 검증
    private void validateAccess(long id, String token, String apiKey) {
        if (apiKey != null && apiKey.equals(this.apiKey))
            return;
        if (token != null && tokenService.validate(token, id))
            return;
        throw new AccessDeniedException("파일에 접근할 권한이 없습니다.");
    }
    
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
    
    @GetMapping("/my")
    public ResponseEntity<List<StoredFileResponse>> findMy(Authentication authentication) {
        return ResponseEntity.ok(storedFileService.findByUploadedBy(authentication.getName()));
    }
    
    @GetMapping("/{id}/view")
    public ResponseEntity<StreamingResponseBody> view(
            @PathVariable long id,
            @RequestParam(required = false) String token,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestHeader HttpHeaders headers) throws IOException {
        validateAccess(id, token, apiKey);
        StoredFile file = storedFileService.findEntityById(id);
        Resource resource = storedFileService.loadResource(file);
        MediaType mediaType = MediaType.parseMediaType(file.getContentType());
        long contentLength = resource.contentLength();
    
        if (headers.getRange().isEmpty()) {
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(contentLength)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_DISPOSITION, inlineDisposition(file.getOriginalFilename()))
                    .body(out -> {
                        try (InputStream in = resource.getInputStream()) {
                            in.transferTo(out);
                        }
                    });
        }
    
        HttpRange range = headers.getRange().getFirst();
        long start = range.getRangeStart(contentLength);
        long end = range.getRangeEnd(contentLength);
        long rangeLength = Math.min(VIDEO_REGION_SIZE, end - start + 1);
        long actualEnd = start + rangeLength - 1;
    
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(mediaType)
                .contentLength(rangeLength)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + actualEnd + "/" + contentLength)
                .body(out -> {
                    try (InputStream in = resource.getInputStream()) {
                        StreamUtils.copyRange(in, out, start, actualEnd);
                    }
                });
    }
    
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(
            @PathVariable long id,
            @RequestParam(required = false) String token,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey) throws IOException {
        validateAccess(id, token, apiKey);
        StoredFile file = storedFileService.findEntityById(id);
        Resource resource = storedFileService.loadResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .contentLength(resource.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION, attachmentDisposition(file.getOriginalFilename()))
                .body(resource);
    }
    
    
    */


    // 파일 업로드 메서드
    // TODO : 파일 업로드 후 반환되는 객체를 DB에 저장하는 로직 추가 필요


    /**
     * 파일 업로드 메서드
     * 
     * @param file 업로드할 파일
     * @return 업로드된 파일의 정보
     * 
     * 응답 예시:
     * <pre>
     *     Long id // 파일 ID
     *     String originalFilename // 원본 파일 이름
     *     String contentType // 파일의 MIME 타입 (예: image/png, application/pdf 등)
     *     String fileType // 파일 유형 (예: IMAGE, DOCUMENT 등)
     *     Long fileSize // 파일 크기 (바이트 단위)
     *     String url // 파일 접근 URL
     *     String viewUrl // 파일 뷰어 URL
     *     String downloadUrl // 파일 다운로드 URL
     *     String uploadedBy // 업로드한 사용자
     *     LocalDateTime uploadedAt // 업로드 시각
     * </pre>
     * 
     *<p> Example response:
     *<p> Uploaded File Details:
     * <pre>
     *     ID: 21
     *     OriginalFilename: "1774443835.png"
     *     ContentType: "image/png"
     *     FileType: "IMAGE"
     *     FileSize: 36878 (bytes)
     *     URL: "https://paste.maerchen.dev/api/storage/files/21/view"
     *     ViewURL: "https://paste.maerchen.dev/api/storage/files/21/view"
     *     DownloadURL: "https://paste.maerchen.dev/api/storage/files/21/download"
     *     UploadedBy: "anonymous"
     *     UploadedAt: "2026-05-18T14:32:53.214345536"
     * </pre>
     */
    public FileDto uploadFile(MultipartFile file, String userId) {
        if (file.isEmpty()) {
            throw new FinalProjectException(ErrorCode.FILE_EMPTY);
        }

        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", multipartResource(file));


            StoredFileResponse fileResponse =
                    restClient.post().uri(fileServerPath).contentType(MediaType.MULTIPART_FORM_DATA)
                            .headers(this::relayAuthorizationHeader).body(body).retrieve()
                            .body(StoredFileResponse.class);
            return insertFileInfoToDatabase(fileResponse, userId);
        } catch (RestClientResponseException e) {
            log.error("파일 서버 업로드 실패. status={}, body={}", e.getStatusCode(),
                    e.getResponseBodyAsString(), e);
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new FinalProjectException(ErrorCode.FILE_TYPE_NOT_SUPPORTED, e);
            } else {
                throw new FinalProjectException(ErrorCode.FILE_UPLOAD_FAILED, e);
            }
        } catch (IOException e) {
            log.error("파일 처리 중 오류 발생", e);
            throw new FinalProjectException(ErrorCode.FILE_UPLOAD_FAILED, e);
        }
    }

    /**
     * MultipartFile을 ByteArrayResource로 변환하여 파일 서버에 업로드할 수 있도록 준비하는 메서드
     * 
     * @param file 업로드할 파일
     * @return ByteArrayResource로 변환된 파일
     * @throws IOException 파일 처리 중 오류 발생 시 예외 발생
     */
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

    // 파일 업로드 후 반환되는 객체를 DB에 저장하는 메서드
    private FileDto insertFileInfoToDatabase(StoredFileResponse fileResponse, String userId) {

        FileDto fileDto = FileDto.builder().atchFileId(001) // 임시값
                .fileServerId(fileResponse.id()).orgnFileNm(fileResponse.originalFilename())
                .savePathNm(fileResponse.url()).saveFileNm(fileResponse.url())
                .fileExtNm(fileResponse.contentType()).fileSizeCnt(fileResponse.fileSize())
                .rgtrId(userId).delYn("N").dwnldCnt(0).build();

        int result = fileUploadMapper.insertFileInfo(fileDto);
        if (result <= 0) {
            log.error("파일 정보 DB 저장 실패: {}", fileDto);
            throw new FinalProjectException(ErrorCode.FILE_INFO_SAVE_FAILED);
        }
        return fileDto;
    }
}
