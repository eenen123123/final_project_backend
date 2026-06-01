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

    private final FileAuthorizationService authorizationService;

    @Value("${file.server.base-url}")
    private String fileServerBaseUrl;

    @Value("${file.server.api-key}")
    private String apiKey;

    @GetMapping("/admin/files/{fileId}/view")
    public void proxyView(@PathVariable long fileId, HttpServletResponse response,
            Authentication authentication) throws IOException {
        // 인가 체크
        if (!authorizationService.canAccess(fileId, authentication)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        HttpURLConnection conn = openFileServerConnection(fileId, "view");
        response.setContentType(conn.getContentType());
        try (InputStream in = conn.getInputStream()) {
            in.transferTo(response.getOutputStream());
        }
    }

    @GetMapping("/admin/files/{fileId}/download")
    public void proxyDownload(@PathVariable long fileId, HttpServletResponse response,
            Authentication authentication) throws IOException {
        // 인가 체크
        if (!authorizationService.canAccess(fileId, authentication)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        HttpURLConnection conn = openFileServerConnection(fileId, "download");
        response.setContentType(conn.getContentType());
        response.setHeader("Content-Disposition", conn.getHeaderField("Content-Disposition"));
        try (InputStream in = conn.getInputStream()) {
            in.transferTo(response.getOutputStream());
        }
    }

    private HttpURLConnection openFileServerConnection(long fileId, String action)
            throws IOException {
        HttpURLConnection conn = (HttpURLConnection) URI
                .create(fileServerBaseUrl + "/api/storage/files/" + fileId + "/" + action).toURL()
                .openConnection();
        conn.setRequestProperty("X-Api-Key", apiKey);
        return conn;
    }

}
