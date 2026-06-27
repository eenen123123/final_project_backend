package kr.or.ddit.controller.notification;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.mapper.notification.NotificationReadMapper;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationReadController {

    private final NotificationReadMapper notificationReadMapper;

    // GET /api/notifications/read - 읽은 알림 ID 목록
    @GetMapping("/read")
    public ResponseEntity<List<String>> getReadNotiIds(Authentication authentication) {
        return ResponseEntity.ok(notificationReadMapper.selectReadNotiIds(authentication.getName()));
    }

    // GET /api/notifications/dismissed - 삭제된 알림 ID 목록
    @GetMapping("/dismissed")
    public ResponseEntity<List<String>> getDismissedNotiIds(Authentication authentication) {
        return ResponseEntity.ok(notificationReadMapper.selectDismissedNotiIds(authentication.getName()));
    }

    // POST /api/notifications/read/{notiId} - 읽음 처리
    @PostMapping("/read/{notiId}")
    public ResponseEntity<Void> markAsRead(@PathVariable String notiId, Authentication authentication) {
        notificationReadMapper.insertReadNoti(authentication.getName(), notiId);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/notifications/{notiId} - 알림 삭제
    @DeleteMapping("/{notiId}")
    public ResponseEntity<Void> dismiss(@PathVariable String notiId, Authentication authentication) {
        notificationReadMapper.dismissNoti(authentication.getName(), notiId);
        return ResponseEntity.noContent().build();
    }
}
