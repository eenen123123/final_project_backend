package kr.or.ddit.finalProject.service.textbook;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.textbook.InventoryStatus;
import kr.or.ddit.finalProject.dto.textbook.TextbookDto;
import kr.or.ddit.finalProject.dto.textbook.TextbookHistoryDto;
import kr.or.ddit.finalProject.dto.textbook.TextbookInventoryDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.textbook.TextbookMapper;
import kr.or.ddit.finalProject.mapper.textbook.TextbookStockMapper;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TextbookServiceImpl implements TextbookService {

    private final TextbookMapper textbookMapper;
    private final TextbookStockMapper textbookStockMapper;

    @Override
    public List<TextbookDto> retrieveTextbookList(PaginationInfo<TextbookDto> paginationInfo) {
        return textbookMapper.selectTextbookList(paginationInfo);
    }

    @Override
    public int retrieveTextbookListCount(PaginationInfo<TextbookDto> paginationInfo) {
        return textbookMapper.countTextbookList(paginationInfo);
    }

    @Override
    public TextbookDto retrieveTextbookBySn(Long textbookSn) {
        TextbookDto textbookDto = textbookMapper.selectTextbookBySn(textbookSn);
        if (textbookDto == null) {
            throw new FinalProjectException(ErrorCode.TEXTBOOK_NOT_FOUND);
        }
        return textbookDto;
    }

    @Override
    @Transactional
    public boolean createTextbook(TextbookDto textbookDto, int initInvtCnt) {
        // 1. 교재 등록
        int result = textbookMapper.insertTextbook(textbookDto);

        // 2. 재고 초기화
        TextbookInventoryDto inventoryDto = TextbookInventoryDto.builder()
                .textbookSn(textbookDto.getTextbookSn()).totInvtCnt(initInvtCnt)
                .salableCnt(initInvtCnt).saleCmplCnt(0).rsrvWaitCnt(0).dmgdDspslCnt(0).minKeepCnt(0)
                .invtStatCd(InventoryStatus.NORMAL)
                .rgtrId(textbookDto.getRgtrId()).lastMdfrId(textbookDto.getLastMdfrId()).build();
        textbookStockMapper.insertInventory(inventoryDto);

        // 3. 입고 내역 등록
        TextbookHistoryDto historyDto =
                TextbookHistoryDto.builder().textbookSn(textbookDto.getTextbookSn()).ioTypeCd("10") // 입고
                        .chgCnt(initInvtCnt).bfrChgCnt(0).aftChgCnt(initInvtCnt)
                        .procUserId(textbookDto.getRgtrId()).build();
        textbookStockMapper.insertHistory(historyDto);

        return result > 0;
    }

    @Override
    @Transactional
    public void modifyTextbook(TextbookDto textbookDto, String currentUserId) {
        TextbookDto original = textbookMapper.selectTextbookBySn(textbookDto.getTextbookSn());
        if (original == null) {
            throw new FinalProjectException(ErrorCode.TEXTBOOK_NOT_FOUND);
        }
        textbookDto.setLastMdfrId(currentUserId);
        textbookMapper.updateTextbook(textbookDto);
    }

    @Override
    @Transactional
    public void removeTextbook(Long textbookSn, String currentUserId) {
        TextbookDto original = textbookMapper.selectTextbookBySn(textbookSn);
        if (original == null) {
            throw new FinalProjectException(ErrorCode.TEXTBOOK_NOT_FOUND);
        }
        textbookMapper.deleteTextbook(textbookSn, currentUserId);
    }

    @Override
    public int retrieveNewTextbookCountThisMonth() {
        return textbookMapper.countNewTextbookThisMonth();
    }

    @Override
    public int retrieveDangerTextbookCount() {
        return textbookMapper.countDangerTextbook();
    }

    @Override
    public int retrieveSoldOutTextbookCount() {
        return textbookMapper.countSoldOutTextbook();
    }

    @Override
    public int retrieveArchivedTextbookCount() {
        return textbookMapper.countArchivedTextbook();
    }

    @Override
    public List<TextbookDto> retrieveTextbookListByCourseSn(Long courseSn) {
        return textbookMapper.selectTextbookListByCourseSn(courseSn);
    }
}
