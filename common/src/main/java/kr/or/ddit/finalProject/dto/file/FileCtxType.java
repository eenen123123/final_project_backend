package kr.or.ddit.finalProject.dto.file;

public enum FileCtxType {
    MEMBER_ROLE, // 회원 역할 관련 파일 id type = String
    CHAT_ROOM, // 채팅방 관련 파일 id type = long
    COURSE, // 강좌 관련 파일 id type = long
    DEPT_DOC, // 부서 문서 관련 파일 id type = String
    INSTRUCTOR,         // 레거시 — 신규 저장 금지
    INSTRUCTOR_BOARD,   // 강사 일반 게시판 첨부파일 (CTX_ID = postSn)
    CLASSROOM_NOTICE,   // 클래스룸 공지사항 첨부파일 (CTX_ID = postSn)
    CLASSROOM_DATAROOM, // 클래스룸 자료실 첨부파일  (CTX_ID = postSn)
    CLASSROOM_ASSIGNMENT, // 클래스룸 과제 제출 첨부파일 (CTX_ID = asgmtSn)
    CLASSROOM_QNA, // 클래스룸 Q&A 첨부파일 (CTX_ID = postSn)
    POST, // React 게시글 관련 파일 id type = long

}
