package kr.or.ddit.finalProject.mapper.notification;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NotificationReadMapper {

    List<String> selectReadNotiIds(@Param("userId") String userId);

    List<String> selectDismissedNotiIds(@Param("userId") String userId);

    int insertReadNoti(@Param("userId") String userId, @Param("notiId") String notiId);

    int dismissNoti(@Param("userId") String userId, @Param("notiId") String notiId);
}
