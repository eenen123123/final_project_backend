package kr.or.ddit.finalProject.dto.classroom;

public enum EnrollStatus {
    ENROLLED ("수강중"),
    COMPLETED("수강완료"),
    WITHDRAWN("중도탈퇴"),
    CANCELLED("등록취소");

    private final String label;

    EnrollStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
