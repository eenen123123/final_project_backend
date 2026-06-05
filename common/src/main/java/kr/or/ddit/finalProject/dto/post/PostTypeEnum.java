package kr.or.ddit.finalProject.dto.post;

public enum PostTypeEnum {
    PERSONAL("개인 쪽지"), SYSTEM("시스템 쪽지");

    private final String description;

    PostTypeEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
