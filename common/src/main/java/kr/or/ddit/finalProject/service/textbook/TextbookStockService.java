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

    // 입/출고 수동 등록 (relDutyTypeCd: 10=재고입고, 20=반품입고, 30=파손폐기, 40=판매출고)
    void addStockHistory(Long textbookSn, int chgCnt, String relDutyTypeCd, String currentUserId);
}
