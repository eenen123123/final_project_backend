package kr.or.ddit.finalProject.service.textbook;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.textbook.RelDutyType;
import kr.or.ddit.finalProject.dto.textbook.TextbookHistoryDto;
import kr.or.ddit.finalProject.dto.textbook.TextbookInventoryDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
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
            throw new FinalProjectException(ErrorCode.INVENTORY_NOT_FOUND);
        }

        // 2. 파손/폐기 증가분만큼 총 재고도 차감
        int originalDmgd = original.getDmgdDspslCnt() != null ? original.getDmgdDspslCnt() : 0;
        int newDmgd      = textbookInventoryDto.getDmgdDspslCnt() != null ? textbookInventoryDto.getDmgdDspslCnt() : 0;
        int dmgdDelta    = newDmgd - originalDmgd;
        if (dmgdDelta > 0) {
            textbookInventoryDto.setTotInvtCnt(
                Math.max(0, textbookInventoryDto.getTotInvtCnt() - dmgdDelta));
        }

        // 3. 입출고 방향 판단 (변경 후 - 변경 전)
        int bfrCnt = original.getTotInvtCnt();
        int aftCnt = textbookInventoryDto.getTotInvtCnt();
        int chgCnt = aftCnt - bfrCnt;
        String ioTypeCd = chgCnt >= 0 ? "10" : "20"; // 10:입고 / 20:출고

        // 4. salableCnt 자동 계산: 총재고 - 판매완료 - 예약대기 - 파손폐기
        int saleCmpl  = original.getSaleCmplCnt() != null ? original.getSaleCmplCnt() : 0;
        int rsrvWait  = original.getRsrvWaitCnt() != null ? original.getRsrvWaitCnt() : 0;
        int newSalable = Math.max(0, aftCnt - saleCmpl - rsrvWait - newDmgd);
        textbookInventoryDto.setSalableCnt(newSalable);
        textbookInventoryDto.setSaleCmplCnt(original.getSaleCmplCnt());
        textbookInventoryDto.setRsrvWaitCnt(original.getRsrvWaitCnt());

        // 4. 재고 수정
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
    @Transactional
    public void addStockHistory(Long textbookSn, int chgCnt, RelDutyType relDutyTypeCd, String currentUserId) {
        TextbookInventoryDto original = textbookStockMapper.selectInventoryByTextbookSn(textbookSn);
        if (original == null) throw new FinalProjectException(ErrorCode.INVENTORY_NOT_FOUND);

        int bfrTot     = original.getTotInvtCnt();
        int saleCmpl   = original.getSaleCmplCnt()  != null ? original.getSaleCmplCnt()  : 0;
        int rsrvWait   = original.getRsrvWaitCnt()  != null ? original.getRsrvWaitCnt()  : 0;
        int dmgd       = original.getDmgdDspslCnt() != null ? original.getDmgdDspslCnt() : 0;

        int aftTot = bfrTot, aftDmgd = dmgd, aftSaleCmpl = saleCmpl;
        String ioTypeCd;

        switch (relDutyTypeCd) {
            case STOCK_IN: case RETURN_IN:
                aftTot = bfrTot + chgCnt;
                ioTypeCd = "10";
                break;
            case DAMAGE:
                aftTot  = Math.max(0, bfrTot - chgCnt);
                aftDmgd = dmgd + chgCnt;
                ioTypeCd = "20";
                break;
            default: // SALE_OUT
                aftSaleCmpl = saleCmpl + chgCnt;
                ioTypeCd = "20";
                break;
        }

        int newSalable = Math.max(0, aftTot - aftSaleCmpl - rsrvWait - aftDmgd);

        TextbookInventoryDto updated = TextbookInventoryDto.builder()
                .textbookSn(textbookSn)
                .totInvtCnt(aftTot)
                .salableCnt(newSalable)
                .saleCmplCnt(aftSaleCmpl)
                .rsrvWaitCnt(original.getRsrvWaitCnt())
                .dmgdDspslCnt(aftDmgd)
                .minKeepCnt(original.getMinKeepCnt())
                .invtStatCd(original.getInvtStatCd())
                .lastMdfrId(currentUserId)
                .build();
        textbookStockMapper.updateInventory(updated);

        TextbookHistoryDto history = TextbookHistoryDto.builder()
                .textbookSn(textbookSn)
                .ioTypeCd(ioTypeCd)
                .chgCnt(chgCnt)
                .bfrChgCnt(bfrTot)
                .aftChgCnt(aftTot)
                .relDutyTypeCd(relDutyTypeCd)
                .procUserId(currentUserId)
                .build();
        textbookStockMapper.insertHistory(history);
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
