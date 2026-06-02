package kr.or.ddit.controller.file;

import jakarta.servlet.http.HttpServletResponse;
import kr.or.ddit.finalProject.service.file.FileAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;

@RequiredArgsConstructor
@RestController
public class AdminFileController {

    // 파일 서버에 저장된 파일에 접근하고 싶으면 이 컨트롤러로 요청을 보내세요
    // 저장된 파일이 응답으로 내려오도록 프록시 역할을 하는 컨트롤러입니다
    private final FileAuthorizationService authorizationService;

    @Value("${file.server.base-url}")
    private String fileServerBaseUrl;

    @Value("${file.server.api-key}")
    private String apiKey;

    @GetMapping("/admin/files/{fileServerId}/view")
    public void proxyView(@PathVariable long fileServerId, HttpServletResponse response,
            Authentication authentication) throws IOException {
        // 인가 체크
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

    @GetMapping("/admin/files/{fileServerId}/download")
    public void proxyDownload(@PathVariable long fileServerId, HttpServletResponse response,
            Authentication authentication) throws IOException {
        // 인가 체크
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
