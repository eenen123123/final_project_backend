package kr.or.ddit.finalProject.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import kr.or.ddit.finalProject.dto.notification.NotificationDto;

@Mapper
public interface NotificationMapper {

    void insertNotification(NotificationDto dto);

    List<NotificationDto> selectUnreadNotifications(@Param("userId") String userId);

    int markAsRead(@Param("notiSn") Long notiSn, @Param("userId") String userId);

    List<NotificationDto> selectAllNotifications(@Param("userId") String userId);
}
