package kr.or.ddit.finalProject.service.textbook;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.textbook.TextbookHistoryDto;
import kr.or.ddit.finalProject.dto.textbook.TextbookInventoryDto;
import kr.or.ddit.finalProject.mapper.textbook.TextbookStockMapper;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TextbookStockServiceImpl implements TextbookStockService {

    private final TextbookStockMapper textbookStockMapper;

    @Override
    public TextbookInventoryDto retrieveInventoryByTextbookSn(Long textbookSn) {
        return textbookStockMapper.selectInventoryByTextbookSn(textbookSn);
    }

    @Override
    @Transactional
    public void modifyInventory(TextbookInventoryDto textbookInventoryDto, String currentUserId) {
        // 1. 변경 전 재고 조회
        TextbookInventoryDto original = textbookStockMapper
                .selectInventoryByTextbookSn(textbookInventoryDto.getTextbookSn());
        if (original == null) {
            throw new IllegalArgumentException("존재하지 않는 재고입니다.");
        }

        // 2. 입출고 방향 판단 (변경 후 - 변경 전)
        int bfrCnt = original.getTotInvtCnt();
        int aftCnt = textbookInventoryDto.getTotInvtCnt();
        int chgCnt = aftCnt - bfrCnt;
        String ioTypeCd = chgCnt >= 0 ? "10" : "20"; // 10:입고 / 20:출고

        // 3. 재고 수정
        textbookInventoryDto.setLastMdfrId(currentUserId);
        textbookStockMapper.updateInventory(textbookInventoryDto);

        // 4. 입출고 내역 등록
        TextbookHistoryDto historyDto =
                TextbookHistoryDto.builder().textbookSn(textbookInventoryDto.getTextbookSn())
                        .ioTypeCd(ioTypeCd).chgCnt(Math.abs(chgCnt)).bfrChgCnt(bfrCnt)
                        .aftChgCnt(aftCnt).procUserId(currentUserId).build();
        textbookStockMapper.insertHistory(historyDto);
    }

    @Override
    public List<TextbookHistoryDto> retrieveHistoryList(
            PaginationInfo<TextbookHistoryDto> paginationInfo) {
        return textbookStockMapper.selectHistoryList(paginationInfo);
    }

    @Override
    public int retrieveHistoryListCount(PaginationInfo<TextbookHistoryDto> paginationInfo) {
        return textbookStockMapper.countHistoryList(paginationInfo);
    }
}
