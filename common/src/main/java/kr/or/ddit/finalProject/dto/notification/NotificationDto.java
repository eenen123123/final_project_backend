package kr.or.ddit.finalProject.dto.notification;

import java.io.Serializable;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto implements Serializable {

    private Long notiSn;
    private String rcvrUserId;
    private String sndrUserId;
    private NotificationType notiType;
    private String notiCn;
    private String linkUrl;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rcptDt;
    private String readYn;
    private LocalDateTime readDt;
}
