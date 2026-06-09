package kr.or.ddit.finalProject.dto.notification;

//@formatter:off
public enum NotificationType {
    CHAT("채팅 메시지"),
    NOTICE("공지사항"),
    POST("쪽지"),
    APPROVAL("결재");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

// @formatter:on