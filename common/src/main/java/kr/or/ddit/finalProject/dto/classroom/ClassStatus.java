package kr.or.ddit.finalProject.dto.classroom;

public enum ClassStatus {
    RECRUITING("모집중"),
    ACTIVE    ("운영중"),
    CLOSED    ("종료"),
    WAITING   ("대기");

    private final String label;

    ClassStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
