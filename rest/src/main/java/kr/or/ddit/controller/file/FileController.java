package kr.or.ddit.controller.file;

import kr.or.ddit.service.file.FileAccessTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileAccessTokenService tokenService;

    @PostMapping("/{fileId}/token")
    public ResponseEntity<Map<String, String>> issueToken(@PathVariable long fileId,
            Authentication authentication) {
        // TODO: 파일 종류에 따른 권한 체크 (강의 구매 여부, 수강 등록 여부 등)
        String viewUrl = tokenService.issueViewUrl(fileId, authentication.getName());
        String downloadUrl = tokenService.issueDownloadUrl(fileId, authentication.getName());
        return ResponseEntity.ok(Map.of("viewUrl", viewUrl, "downloadUrl", downloadUrl));
    }
}
