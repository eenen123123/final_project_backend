package kr.or.ddit.finalProject.service.board.notice;

import java.util.List;

import kr.or.ddit.finalProject.dto.board.NoticeDto;

public interface NoticeService {

    /**
     * 공지사항 목록 조회
     *
     * @param noticeTypeCd 공지 유형 코드 (CL_CODE: 103), null이면 전체 조회
     * @return 공지사항 목록
     */
    List<NoticeDto> getNoticeList(String noticeTypeCd);

    /**
     * 공지사항 단건 조회
     *
     * @param postSn 공지사항 PK
     * @return 공지사항 상세 정보
     */
    NoticeDto getNoticeById(Long postSn);

    /**
     * 공지사항 등록
     *
     * @param noticeDto 등록할 공지사항 정보
     */
    void createNotice(NoticeDto noticeDto);

    /**
     * 공지사항 수정
     *
     * @param noticeDto 수정할 공지사항 정보
     */
    void updateNotice(NoticeDto noticeDto);

    /**
     * 공지사항 삭제
     *
     * @param postSn 삭제할 공지사항 PK
     */
    void deleteNotice(Long postSn);

}
