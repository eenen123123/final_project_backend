package kr.or.ddit.finalProject.service.file;

import java.io.IOException;
import java.util.List;
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
import kr.or.ddit.finalProject.mapper.FileUploadMapper;
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
    private final FileUploadMapper fileUploadMapper;

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
    /**
     * 기존 호출부(ChatMessageController 등) 하위 호환용 오버로드입니다.
     * atchFileId를 지정하지 않는 기존 코드는 이 메서드를 그대로 사용하시면 됩니다.
     *
     * [변경 이유] 강좌 자료 관리(/instructor/course/materials) 기능 추가 시,
     * 파일을 특정 강좌의 파일 그룹(CMMT_ATCH_FILE_CL)에 귀속시켜야 했습니다.
     * 기존 uploadFile은 atchFileId를 001(임시값)으로 하드코딩했는데,
     * 이를 atchFileId를 인수로 받는 오버로드로 분리하고 기존 메서드는 1을 넘기도록 변경했습니다.
     */
    public FileDto uploadFile(MultipartFile file, String userId) {
        return uploadFile(file, userId, 1);
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
     */
    public FileDto uploadFile(MultipartFile file, String userId, int atchFileId) {
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
            return insertFileInfoToDatabase(fileResponse, userId, atchFileId);
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
    private FileDto insertFileInfoToDatabase(StoredFileResponse fileResponse, String userId, int atchFileId) {

        FileDto fileDto = FileDto.builder().atchFileId(atchFileId)
                .orgnFileNm(fileResponse.originalFilename()).savePathNm(fileResponse.url())
                .saveFileNm(fileResponse.url()).fileExtNm(fileResponse.contentType())
                .fileSizeCnt(fileResponse.fileSize()).rgtrId(userId).delYn("N").dwnldCnt(0).build();

        int result = fileUploadMapper.insertFileInfo(fileDto);
        if (result <= 0) {
            log.error("파일 정보 DB 저장 실패: {}", fileDto);
            throw new FinalProjectException(ErrorCode.FILE_INFO_SAVE_FAILED);
        }
        return fileDto;
    }
}
