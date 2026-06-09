package kr.or.ddit.controller;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import kr.or.ddit.finalProject.dto.notification.NotificationDto;
import kr.or.ddit.finalProject.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationDto> getNotifications(Authentication authentication) {
        return notificationService.getAllNotifications(authentication.getName());
    }

    @PostMapping("/{notiSn}/read")
    public void markAsRead(@PathVariable Long notiSn, Authentication authentication) {
        notificationService.markAsRead(notiSn, authentication.getName());
    }
}
