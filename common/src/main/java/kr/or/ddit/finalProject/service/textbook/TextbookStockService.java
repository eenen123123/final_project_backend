package kr.or.ddit.finalProject.service.textbook;

import java.util.List;

import kr.or.ddit.finalProject.dto.textbook.TextbookHistoryDto;
import kr.or.ddit.finalProject.dto.textbook.TextbookInventoryDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

public interface TextbookStockService {

    // 재고 단건 조회
    TextbookInventoryDto retrieveInventoryByTextbookSn(Long textbookSn);

    // 재고 수정 (입출고 내역 자동 생성)
    void modifyInventory(TextbookInventoryDto textbookInventoryDto, String currentUserId);

    // 입출고 내역 목록 조회 (페이징 + 검색)
    List<TextbookHistoryDto> retrieveHistoryList(PaginationInfo<TextbookHistoryDto> paginationInfo);

    // 입출고 내역 총 개수 (페이징용)
    int retrieveHistoryListCount(PaginationInfo<TextbookHistoryDto> paginationInfo);
}
