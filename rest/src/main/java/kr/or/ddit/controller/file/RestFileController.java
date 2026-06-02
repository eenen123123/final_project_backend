package kr.or.ddit.controller.file;

import kr.or.ddit.finalProject.dto.file.FileCtxType;
import kr.or.ddit.finalProject.dto.file.FileDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.service.file.FileAuthorizationService;
import kr.or.ddit.finalProject.service.file.FileUploadService;
import kr.or.ddit.service.file.FileAccessTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import org.springframework.web.multipart.MultipartFile;


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
            throw new FinalProjectException(ErrorCode.FILE_NOT_FOUND);
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

        FileDto fileDto = fileUploadService.uploadFile(file, userId, ctxTypeEnum, ctxId);
        return ResponseEntity.ok(fileDto.getFileServerId());

        /*
            React에서 게시글을 작성할 때 파일을 업로드 하는 경우,
            1. 게시글 작성 폼에서 파일을 선택하면 즉시 이 API로 파일을 업로드
            2. 업로드 성공 시, 서버에서 발급한 파일 ID를 React 상태에 저장
            3. 게시글 작성 완료 시, 파일 ID를 포함하여 게시글 작성 API 호출
                배열로 첨부 파일 ID를 보내면 게시글과 파일을 연결하는 작업은 게시글 작성 API에서 처리
        */
    }



    @PostMapping("/{fileId}/token")
    public ResponseEntity<Map<String, String>> issueToken(@PathVariable long fileId,
            Authentication authentication) {
        // TODO: 파일 종류에 따른 권한 체크 (강의 구매 여부, 수강 등록 여부 등)
        if (!authorizationService.canAccess(fileId, authentication)) {
            return ResponseEntity.status(403).build();
        }

        String viewUrl = tokenService.issueViewUrl(fileId, authentication.getName());
        String downloadUrl = tokenService.issueDownloadUrl(fileId, authentication.getName());
        return ResponseEntity.ok(Map.of("viewUrl", viewUrl, "downloadUrl", downloadUrl));
    }
}
