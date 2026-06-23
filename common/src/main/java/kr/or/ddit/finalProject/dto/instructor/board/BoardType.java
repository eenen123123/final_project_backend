package kr.or.ddit.finalProject.dto.instructor.board;

/**
 * 강사 게시판 분류 코드 열거형.
 * INSTRUCTOR_BOARD.BOARD_TYPE_CD 컬럼 값과 1:1 대응하며,
 * 화면 표시용 한글 레이블(label)을 함께 제공한다.
 */
public enum BoardType {

    /** 공지사항 */
    NOTICE("공지사항"),

    /** 질문/답변 — 학생이 작성, 강사가 답변 (강사가 직접 작성 불가) */
    QNA("Q&A"),

    /** 자료실 */
    DATAROOM("자료실");

    private final String label;

    BoardType(String label) { this.label = label; }

    /** 화면 표시용 한글 레이블 반환 */
    public String getLabel() { return label; }
}
