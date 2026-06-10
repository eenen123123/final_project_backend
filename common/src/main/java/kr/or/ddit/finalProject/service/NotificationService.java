package kr.or.ddit.finalProject.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kr.or.ddit.finalProject.dto.notification.NotificationDto;
import kr.or.ddit.finalProject.dto.notification.NotificationType;
import kr.or.ddit.finalProject.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String NOTIFICATION_TOPIC_PREFIX = "/topic/notifications/";

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationMapper notificationMapper;

    @Transactional
    public void sendNotification(String rcvrUserId, String sndrUserId, NotificationType notiType,
            String notiCn, String linkUrl) {
        NotificationDto dto = NotificationDto.builder().rcvrUserId(rcvrUserId)
                .sndrUserId(sndrUserId).notiType(notiType).notiCn(notiCn).linkUrl(linkUrl)
                .rcptDt(LocalDateTime.now()).readYn("N").build();
        notificationMapper.insertNotification(dto);
        messagingTemplate.convertAndSend(NOTIFICATION_TOPIC_PREFIX + rcvrUserId, dto);
    }

    public List<NotificationDto> getUnreadNotifications(String userId) {
        return notificationMapper.selectUnreadNotifications(userId);
    }

    public List<NotificationDto> getAllNotifications(String userId) {
        return notificationMapper.selectAllNotifications(userId);
    }

    @Transactional
    public void markAsRead(Long notiSn, String userId) {
        notificationMapper.markAsRead(notiSn, userId);
    }
}
