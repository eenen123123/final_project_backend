package kr.or.ddit.finalProject.dto.instructor.board;

public enum BoardType {
    NOTICE("공지사항"),
    QNA("Q&A"),
    DATAROOM("자료실");

    private final String label;

    BoardType(String label) { this.label = label; }
    public String getLabel() { return label; }
}
