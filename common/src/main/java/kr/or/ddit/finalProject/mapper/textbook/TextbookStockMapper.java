package kr.or.ddit.finalProject.mapper.textbook;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.textbook.TextbookHistoryDto;
import kr.or.ddit.finalProject.dto.textbook.TextbookInventoryDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

@Mapper
public interface TextbookStockMapper {

    // 재고 단건 조회
    TextbookInventoryDto selectInventoryByTextbookSn(@Param("textbookSn") Long textbookSn);

    // 재고 등록 (교재 등록 시 초기화)
    int insertInventory(TextbookInventoryDto textbookInventoryDto);

    // 재고 수정
    int updateInventory(TextbookInventoryDto textbookInventoryDto);

    // 입출고 내역 등록
    int insertHistory(TextbookHistoryDto textbookHistoryDto);

    // 입출고 내역 목록 조회 (페이징 + 검색)
    List<TextbookHistoryDto> selectHistoryList(PaginationInfo<TextbookHistoryDto> paginationInfo);

    // 입출고 내역 총 개수 (페이징용)
    int countHistoryList(PaginationInfo<TextbookHistoryDto> paginationInfo);
}
