package kr.or.ddit.controller.textbook;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.or.ddit.finalProject.dto.textbook.TextbookDto;
import kr.or.ddit.finalProject.dto.textbook.TextbookHistoryDto;
import kr.or.ddit.finalProject.dto.textbook.TextbookInventoryDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.service.textbook.TextbookService;
import kr.or.ddit.finalProject.service.textbook.TextbookStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin/textbook/stock")
@RequiredArgsConstructor
public class TextbookStockController {

    private final TextbookStockService textbookStockService;
    private final TextbookService textbookService;

    // 재고 전체 목록
    @GetMapping
    public String stockList(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long subjClId,
            Model model) {
        PaginationInfo<TextbookDto> paginationInfo = new PaginationInfo<>(size, 5, page);
        TextbookDto condition = TextbookDto.builder()
                .keyword(keyword).subjClId(subjClId).sort("stock").build();
        paginationInfo.setDetailCondition(condition);
        int totalCount = textbookService.retrieveTextbookListCount(paginationInfo);
        List<TextbookDto> textbookList = textbookService.retrieveTextbookList(paginationInfo);
        model.addAttribute("textbookList", textbookList);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("paginationInfo", paginationInfo);
        model.addAttribute("dangerCount", textbookService.retrieveDangerTextbookCount());
        model.addAttribute("soldOutCount", textbookService.retrieveSoldOutTextbookCount());
        return "admin:/textbook/textbook_stock_list";
    }

    // 재고 상세 + 입출고 내역 조회
    @GetMapping("/{textbookSn}")
    public String stockDetail(@PathVariable Long textbookSn,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String ioTypeCd,
            @RequestParam(required = false) String startDt,
            @RequestParam(required = false) String endDt, Model model) {

        // 재고 조회
        TextbookInventoryDto inventoryDto =
                textbookStockService.retrieveInventoryByTextbookSn(textbookSn);

        // 입출고 내역 전체 조회 (클라이언트 사이드 페이징)
        PaginationInfo<TextbookHistoryDto> paginationInfo = new PaginationInfo<>(9999, 1, 1);
        TextbookHistoryDto condition = TextbookHistoryDto.builder().textbookSn(textbookSn)
                .keyword(keyword).ioTypeCd(ioTypeCd).startDt(startDt).endDt(endDt).build();
        paginationInfo.setDetailCondition(condition);

        List<TextbookHistoryDto> historyList =
                textbookStockService.retrieveHistoryList(paginationInfo);

        model.addAttribute("inventoryDto", inventoryDto);
        model.addAttribute("historyList", historyList);
        model.addAttribute("textbookSn", textbookSn);

        return "admin:/textbook/textbook_stock";
    }

    // 입/출고 수동 등록
    @PostMapping("/{textbookSn:\\d+}/history")
    public String addHistory(@PathVariable Long textbookSn,
            @RequestParam int chgCnt,
            @RequestParam String relDutyTypeCd,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        textbookStockService.addStockHistory(textbookSn, chgCnt, relDutyTypeCd, authentication.getName());
        redirectAttributes.addFlashAttribute("successMsg", "입출고 내역이 등록되었습니다.");
        return "redirect:/admin/textbook/stock/" + textbookSn;
    }

    // 재고 수정 처리
    @PostMapping("/{textbookSn}")
    public String stockUpdate(@PathVariable Long textbookSn,
            TextbookInventoryDto textbookInventoryDto, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        textbookInventoryDto.setTextbookSn(textbookSn);
        textbookStockService.modifyInventory(textbookInventoryDto, authentication.getName());
        redirectAttributes.addFlashAttribute("successMsg", "재고가 수정되었습니다.");
        return "redirect:/admin/textbook/stock/" + textbookSn;
    }
}
