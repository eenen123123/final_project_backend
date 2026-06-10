package kr.or.ddit.controller.file;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import kr.or.ddit.finalProject.dto.file.FileCtxType;
import kr.or.ddit.finalProject.dto.file.FileDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.service.file.FileAuthorizationService;
import kr.or.ddit.finalProject.service.file.FileUploadService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class AdminFileController {

    private final FileAuthorizationService authorizationService;
    private final FileUploadService fileUploadService;

    @Value("${file.server.base-url}")
    private String fileServerBaseUrl;

    @Value("${file.server.api-key}")
    private String apiKey;

    @PostMapping("/api/admin/lecture/video/upload")
    public ResponseEntity<Long> uploadLectureVideo(@RequestParam("file") MultipartFile file,
            @RequestParam("courseSn") String courseSn,
            Authentication authentication) {

        if (file == null || file.isEmpty()) {
            throw new FinalProjectException(ErrorCode.FILE_EMPTY);
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("video/")) {
            throw new FinalProjectException(ErrorCode.INVALID_FILE_TYPE);
        }

        FileDto fileDto = fileUploadService.uploadVideoFile(file, authentication.getName(),
                FileCtxType.COURSE, courseSn);
        return ResponseEntity.ok(fileDto.getFileServerId());
    }

    // TipTap 에디터 파일 업로드
    @PostMapping("/api/files/upload")
    public ResponseEntity<Long> uploadFile(@RequestParam("file") MultipartFile file,
            @RequestParam("ctxType") String ctxType, @RequestParam("ctxId") String ctxId,
            Authentication authentication) {

        if (file == null || file.isEmpty()) {
            throw new FinalProjectException(ErrorCode.FILE_EMPTY);
        }
        if (file.getContentType() == null || (!file.getContentType().startsWith("image/")
                && !file.getContentType().equals("application/pdf"))) {
            throw new FinalProjectException(ErrorCode.INVALID_FILE_TYPE);
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new FinalProjectException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        FileCtxType ctxTypeEnum;
        try {
            ctxTypeEnum = FileCtxType.valueOf(ctxType);
        } catch (IllegalArgumentException e) {
            throw new FinalProjectException(ErrorCode.INVALID_FILE_CONTEXT);
        }

        FileDto fileDto =
                fileUploadService.uploadFile(file, authentication.getName(), ctxTypeEnum, ctxId);
        return ResponseEntity.ok(fileDto.getFileServerId());
    }


    // 파일 프록시 뷰
    @GetMapping("/admin/files/{fileServerId}/view")
    public void proxyView(@PathVariable long fileServerId, HttpServletResponse response,
            Authentication authentication) throws IOException {
        if (!authorizationService.canAccess(fileServerId, authentication)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        HttpURLConnection conn = openFileServerConnection(fileServerId, "view");
        response.setContentType(conn.getContentType());
        try (InputStream in = conn.getInputStream()) {
            in.transferTo(response.getOutputStream());
        }
    }

    // 파일 프록시 다운로드
    @GetMapping("/admin/files/{fileServerId}/download")
    public void proxyDownload(@PathVariable long fileServerId, HttpServletResponse response,
            Authentication authentication) throws IOException {
        if (!authorizationService.canAccess(fileServerId, authentication)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        HttpURLConnection conn = openFileServerConnection(fileServerId, "download");
        response.setContentType(conn.getContentType());
        response.setHeader("Content-Disposition", conn.getHeaderField("Content-Disposition"));
        try (InputStream in = conn.getInputStream()) {
            in.transferTo(response.getOutputStream());
        }
    }

    private HttpURLConnection openFileServerConnection(long fileServerId, String action)
            throws IOException {
        HttpURLConnection conn = (HttpURLConnection) URI
                .create(fileServerBaseUrl + "/api/storage/files/" + fileServerId + "/" + action)
                .toURL().openConnection();
        conn.setRequestProperty("X-Api-Key", apiKey);
        return conn;
    }
}
