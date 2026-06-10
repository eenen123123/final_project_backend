package kr.or.ddit.finalProject.service.textbook;

import java.util.List;

import kr.or.ddit.finalProject.dto.textbook.TextbookDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

public interface TextbookService {

    // 교재 목록 조회 (페이징 + 필터)
    List<TextbookDto> retrieveTextbookList(PaginationInfo<TextbookDto> paginationInfo);

    // 교재 총 개수 (페이징용)
    int retrieveTextbookListCount(PaginationInfo<TextbookDto> paginationInfo);

    // 교재 단건 조회
    TextbookDto retrieveTextbookBySn(Long textbookSn);

    // 교재 등록 (재고 초기화 포함)
    boolean createTextbook(TextbookDto textbookDto, int initInvtCnt);

    // 교재 수정
    void modifyTextbook(TextbookDto textbookDto, String currentUserId);

    // 교재 삭제 (논리 삭제)
    void removeTextbook(Long textbookSn, String currentUserId);

    int retrieveNewTextbookCountThisMonth();

    int retrieveDangerTextbookCount();

    int retrieveSoldOutTextbookCount();
}
