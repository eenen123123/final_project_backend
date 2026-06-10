package kr.or.ddit.finalProject.service.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
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
import kr.or.ddit.finalProject.dto.file.FileCtxType;
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
    private final RestClient videoRestClient = buildVideoRestClient();

    private static RestClient buildVideoRestClient() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30_000);
        factory.setReadTimeout(0); // 타임아웃 없음 (대용량 영상 업로드)
        return RestClient.builder().requestFactory(factory).build();
    }
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
     * <pre>
     * 기존 호출부(ChatMessageController 등) 하위 호환용 오버로드입니다.
     * atchFileId를 지정하지 않는 기존 코드는 이 메서드를 그대로 사용하시면 됩니다.
     *
     * [변경 이유] 강좌 자료 관리(/instructor/course/materials) 기능 추가 시,
     * 파일을 특정 강좌의 파일 그룹(CMMT_ATCH_FILE_CL)에 귀속시켜야 했습니다.
     * 기존 uploadFile은 atchFileId를 001(임시값)으로 하드코딩했는데,
     * 이를 atchFileId를 인수로 받는 오버로드로 분리하고 기존 메서드는 1을 넘기도록 변경했습니다.
     * </pre>
     * 
     * 파일 업로드 메서드
     * 
     * @param file 업로드할 파일
     * @param userId 업로드한 사용자 ID
     * @param ctxType 파일이 속한 컨텍스트 유형 (예: COURSE, LECTURE 등)
     * @param ctxId 파일이 속한 컨텍스트 ID (예: COURSE_ID, LECTURE_ID 등, PK값)
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
    public FileDto uploadFile(MultipartFile file, String userId, FileCtxType ctxType,
            String ctxId) {
        return uploadFile(file, userId, 1, ctxType, ctxId);
    }

    /** 관리자 전용 영상 업로드. 영상 magic bytes 검증 후 파일 서버에 저장한다. */
    public FileDto uploadVideoFile(MultipartFile file, String userId, FileCtxType ctxType,
            String ctxId) {
        if (file.isEmpty()) {
            throw new FinalProjectException(ErrorCode.FILE_EMPTY);
        }
        try {
            validateVideoMagicBytes(file);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", streamingResource(file));

            StoredFileResponse fileResponse =
                    videoRestClient.post().uri(fileServerPath).contentType(MediaType.MULTIPART_FORM_DATA)
                            .headers(this::relayAuthorizationHeader).body(body).retrieve()
                            .body(StoredFileResponse.class);
            log.info("영상 파일 서버 업로드 성공: {}", fileResponse);
            return insertFileInfoToDatabase(fileResponse, userId, 1, ctxType, ctxId);
        } catch (RestClientResponseException e) {
            log.error("영상 파일 서버 업로드 실패. status={}, body={}", e.getStatusCode(),
                    e.getResponseBodyAsString(), e);
            throw new FinalProjectException(ErrorCode.FILE_UPLOAD_FAILED, e);
        } catch (IOException e) {
            log.error("영상 파일 처리 중 오류 발생", e);
            throw new FinalProjectException(ErrorCode.FILE_UPLOAD_FAILED, e);
        }
    }

    /**
     * atchFileId를 명시적으로 지정해 파일을 특정 그룹에 귀속시키는 업로드 메서드입니다.
     *
     * [추가 이유] 강좌 자료 관리에서 CMMT_ATCH_FILE_DTL.ATCH_FILE_ID에는
     * CMMT_ATCH_FILE_CL의 PK가 들어가야 합니다(FK 제약조건이 존재합니다).
     * 강좌마다 파일 그룹을 만들고 그 ID를 COURSE.ATCH_FILE_ID에 저장한 뒤,
     * 이 메서드를 호출해 해당 그룹 ID로 파일을 저장합니다.
     *
     * @param atchFileId CMMT_ATCH_FILE_CL.ATCH_FILE_ID (파일 그룹 PK)
     * @param ctxType 파일이 속한 컨텍스트 유형 (예: COURSE, LECTURE 등)
     * @param ctxId 파일이 속한 컨텍스트 ID (예: COURSE_ID, LECTURE_ID 등, PK값)
     */
    public FileDto uploadFile(MultipartFile file, String userId, int atchFileId,
            FileCtxType ctxType, String ctxId

    ) {
        if (file.isEmpty()) {
            throw new FinalProjectException(ErrorCode.FILE_EMPTY);
        }

        try {
            validateMagicBytes(file);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", multipartResource(file));

            StoredFileResponse fileResponse =
                    restClient.post().uri(fileServerPath).contentType(MediaType.MULTIPART_FORM_DATA)
                            .headers(this::relayAuthorizationHeader).body(body).retrieve()
                            .body(StoredFileResponse.class);
            log.info("파일 서버 업로드 성공: {}", fileResponse);
            return insertFileInfoToDatabase(fileResponse, userId, atchFileId, ctxType, ctxId);
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
     * CMMT_ATCH_FILE_CL에 새 파일 그룹 행을 삽입하고 생성된 ID를 반환합니다.
     *
     * [추가 이유] CMMT_ATCH_FILE_DTL.ATCH_FILE_ID는 CMMT_ATCH_FILE_CL.ATCH_FILE_ID를
     * 참조하는 FK이므로, 파일을 저장하기 전에 반드시 그룹 행이 먼저 존재해야 합니다.
     * CMMT_ATCH_FILE_CL.ATCH_FILE_ID는 IDENTITY 컬럼이 아니어서
     * SEQ_CMMT_ATCH_FILE_CL 시퀀스로 채번한 뒤 직접 INSERT합니다.
     *
     * @return 새로 생성된 CMMT_ATCH_FILE_CL.ATCH_FILE_ID
     */
    public int createFileGroup() {
        int groupId = fileUploadMapper.selectNextFileGroupId();
        fileUploadMapper.insertFileGroup(groupId);
        return groupId;
    }

    /**
     * 특정 파일 그룹에 속한 파일 목록을 조회합니다 (DEL_YN='N'인 것만).
     *
     * [추가 이유] 강좌 자료 관리에서 강좌별 자료 목록을 조회하기 위해 추가했습니다.
     * groupId = COURSE.ATCH_FILE_ID (강좌에 귀속된 파일 그룹 ID)
     */
    public List<FileDto> retrieveFilesByGroupId(int groupId) {
        return fileUploadMapper.selectFilesByGroupId(groupId);
    }

    /**
     * CMMT_ATCH_FILE_DTL 행을 논리 삭제(DEL_YN='Y')합니다.
     *
     * [추가 이유] 강좌 자료 관리에서 파일 삭제 기능 구현을 위해 추가했습니다.
     * 물리 삭제 시 외부 파일 서버의 파일은 그대로 남으므로 논리 삭제 방식을 선택했습니다.
     */
    public void removeFile(Integer atchFileDtlSn, String userId) {
        int result = fileUploadMapper.softDeleteFile(atchFileDtlSn, userId);
        if (result <= 0) {
            throw new FinalProjectException(ErrorCode.FILE_INFO_SAVE_FAILED);
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

    private Resource streamingResource(MultipartFile file) throws IOException {
        return new InputStreamResource(file.getInputStream()) {
            @Override
            public String getFilename() { return file.getOriginalFilename(); }
            @Override
            public long contentLength() { return file.getSize(); }
        };
    }

    // Content-Type 헤더는 클라이언트가 조작 가능하므로 파일 시그니처(Magic Bytes)로 실제 포맷을 검증
    private void validateMagicBytes(MultipartFile file) {
        byte[] header = new byte[12];
        int read;
        try (InputStream in = file.getInputStream()) {
            read = in.read(header);
        } catch (IOException e) {
            throw new FinalProjectException(ErrorCode.FILE_READ_ERROR, e);
        }
        if (read < 4 || !isSupportedFormat(header, read)) {
            throw new FinalProjectException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    private void validateVideoMagicBytes(MultipartFile file) {
        byte[] header = new byte[12];
        int read;
        try (InputStream in = file.getInputStream()) {
            read = in.read(header);
        } catch (IOException e) {
            throw new FinalProjectException(ErrorCode.FILE_READ_ERROR, e);
        }
        if (read < 4 || !isVideoFormat(header, read)) {
            throw new FinalProjectException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    private boolean isSupportedFormat(byte[] b, int len) {
        // JPEG: FF D8 FF
        if (len >= 3 && b[0] == (byte) 0xFF && b[1] == (byte) 0xD8 && b[2] == (byte) 0xFF) return true;
        // PNG: 89 50 4E 47
        if (len >= 4 && b[0] == (byte) 0x89 && b[1] == 0x50 && b[2] == 0x4E && b[3] == 0x47) return true;
        // GIF: GIF8
        if (len >= 4 && b[0] == 0x47 && b[1] == 0x49 && b[2] == 0x46 && b[3] == 0x38) return true;
        // WEBP: RIFF....WEBP
        if (len >= 12 && b[0] == 0x52 && b[1] == 0x49 && b[2] == 0x46 && b[3] == 0x46
                && b[8] == 0x57 && b[9] == 0x45 && b[10] == 0x42 && b[11] == 0x50) return true;
        // PDF: %PDF
        return len >= 4 && b[0] == 0x25 && b[1] == 0x50 && b[2] == 0x44 && b[3] == 0x46;
    }

    private boolean isVideoFormat(byte[] b, int len) {
        // MP4/MOV: ftyp box at offset 4
        if (len >= 8 && b[4] == 0x66 && b[5] == 0x74 && b[6] == 0x79 && b[7] == 0x70) return true;
        // AVI: RIFF....AVI
        if (len >= 12 && b[0] == 0x52 && b[1] == 0x49 && b[2] == 0x46 && b[3] == 0x46
                && b[8] == 0x41 && b[9] == 0x56 && b[10] == 0x49 && b[11] == 0x20) return true;
        // MKV/WebM: 1A 45 DF A3
        return len >= 4 && b[0] == 0x1A && b[1] == 0x45 && b[2] == (byte) 0xDF && b[3] == (byte) 0xA3;
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

    /**
     * [변경 이유] 기존에는 atchFileId가 001(=1)로 하드코딩되어 있었습니다.
     * 강좌 자료 관리 기능에서 파일 그룹 ID를 호출부에서 결정해야 했으므로
     * atchFileId를 파라미터로 받도록 변경했습니다.
     * 기존 uploadFile(file, userId) 오버로드는 1을 넘겨 이전 동작을 유지합니다.
     */
    private FileDto insertFileInfoToDatabase(StoredFileResponse fileResponse, String userId,
            int atchFileId, FileCtxType ctxType, String ctxId

    ) {

        FileDto fileDto = FileDto.builder().atchFileId(atchFileId)
                .orgnFileNm(fileResponse.originalFilename()).savePathNm(fileResponse.url())
                .saveFileNm(fileResponse.url()).fileExtNm(fileResponse.contentType())
                .fileServerId(fileResponse.id()).fileSizeCnt(fileResponse.fileSize()).rgtrId(userId)
                .delYn("N").dwnldCnt(0).fileCtxType(ctxType).fileCtxId(ctxId).build();

        int result = fileUploadMapper.insertFileInfo(fileDto);
        if (result <= 0) {
            log.error("파일 정보 DB 저장 실패: {}", fileDto);
            throw new FinalProjectException(ErrorCode.FILE_INFO_SAVE_FAILED);
        }
        return fileDto;
    }
}
