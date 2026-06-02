package kr.or.ddit.service.file;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileAccessTokenService {

    private final StringRedisTemplate redisTemplate;

    @Value("${file.server.base-url}")
    private String fileServerBaseUrl;

    private static final Duration TOKEN_TTL = Duration.ofSeconds(60 * 10); // 10분
    private static final String KEY_PREFIX = "file:token:";

    public String issueViewUrl(long fileId, String userId) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(KEY_PREFIX + token, fileId + ":" + userId, TOKEN_TTL);
        return fileServerBaseUrl + "/api/storage/files/" + fileId + "/view?token=" + token;
    }

    public String issueDownloadUrl(long fileId, String userId) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(KEY_PREFIX + token, fileId + ":" + userId, TOKEN_TTL);
        return fileServerBaseUrl + "/api/storage/files/" + fileId + "/download?token=" + token;
    }
}
