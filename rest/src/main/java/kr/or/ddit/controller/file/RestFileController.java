package kr.or.ddit.controller.file;

import kr.or.ddit.finalProject.dto.file.FileCtxType;
import kr.or.ddit.finalProject.dto.file.FileDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.service.file.FileAuthorizationService;
import kr.or.ddit.finalProject.service.file.FileUploadService;
import kr.or.ddit.service.file.FileAccessTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;



@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class RestFileController {

    private final FileUploadService fileUploadService;
    private final FileAccessTokenService tokenService;
    private final FileAuthorizationService authorizationService;



    @PostMapping("/upload")
    public ResponseEntity<Long> uploadFile(@RequestParam("file") MultipartFile file,
            @RequestParam("ctxType") String ctxType, @RequestParam("ctxId") String ctxId,
            Authentication authentication) {

        String userId = authentication.getName();
        // 업로드 권한 인증은 필터에서 처리 (로그인 한 사용자만 접근 가능)

        if (file == null || file.isEmpty()) {
            throw new FinalProjectException(ErrorCode.FILE_EMPTY);
        }

        if (file.getContentType() == null || (!file.getContentType().startsWith("image/")
                && !file.getContentType().equals("application/pdf"))) {
            throw new FinalProjectException(ErrorCode.INVALID_FILE_TYPE);
        }

        if (file.getSize() > 10 * 1024 * 1024) { // 10MB
            throw new FinalProjectException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        FileCtxType ctxTypeEnum;
        try {
            ctxTypeEnum = FileCtxType.valueOf(ctxType);
        } catch (IllegalArgumentException e) {
            throw new FinalProjectException(ErrorCode.INVALID_FILE_CONTEXT);
        }

        /*
            파일 업로드 흐름:
            1. 파일 선택 즉시 이 API로 업로드
            2. 응답으로 받은 파일 ID를 React 상태에 저장
            3. 게시글 저장 시 파일 ID 배열을 함께 전송 → 게시글 작성 API에서 연결 처리
        */
        FileDto fileDto = fileUploadService.uploadFile(file, userId, ctxTypeEnum, ctxId);
        return ResponseEntity.ok(fileDto.getFileServerId());
    }



    @PostMapping("/{fileId}/token")
    public ResponseEntity<Map<String, String>> issueToken(@PathVariable long fileId,
            Authentication authentication) {
        log.info("파일 ID: {}, 사용자: {}", fileId, authentication.getName());
        // TODO: 파일 종류에 따른 권한 체크 (강의 구매 여부, 수강 등록 여부 등)
        if (!authorizationService.canAccess(fileId, authentication)) {
            throw new FinalProjectException(ErrorCode.FILE_ACCESS_DENIED);
        }

        String viewUrl = tokenService.issueViewUrl(fileId, authentication.getName());
        return ResponseEntity.ok(Map.of("viewUrl", viewUrl));
    }

    @PostMapping("/tokens")
    public ResponseEntity<Map<Long, String>> issueTokens(@RequestBody Map<String, long[]> request,
            Authentication authentication) {

        log.info("파일 ID 목록: {}", (Object) request.get("ids"));

        long[] fileIds = request.get("ids");
        if (fileIds == null || fileIds.length == 0) {
            throw new FinalProjectException(ErrorCode.FILE_IDS_REQUIRED);
        }
        if (fileIds.length > 5) {
            throw new FinalProjectException(ErrorCode.FILE_IDS_LIMIT_EXCEEDED);
        }

        if (!authorizationService.canAccess(fileIds, authentication)) {
            throw new FinalProjectException(ErrorCode.FILE_ACCESS_DENIED);
        }
        String userId = authentication.getName();
        Map<Long, String> tokens = Arrays.stream(fileIds).boxed().collect(Collectors
                .toMap(fileId -> fileId, fileId -> tokenService.issueViewUrl(fileId, userId)));

        return ResponseEntity.ok(tokens);
    }

}
