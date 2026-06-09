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

import kr.or.ddit.finalProject.dto.textbook.TextbookHistoryDto;
import kr.or.ddit.finalProject.dto.textbook.TextbookInventoryDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.service.textbook.TextbookStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin/textbook/stock")
@RequiredArgsConstructor
public class TextbookStockController {

    private final TextbookStockService textbookStockService;

    // 재고 상세 + 입출고 내역 조회
    @GetMapping("/{textbookSn}")
    public String stockDetail(@PathVariable Long textbookSn,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String ioTypeCd,
            @RequestParam(required = false) String startDt,
            @RequestParam(required = false) String endDt, Model model) {

        // 재고 조회
        TextbookInventoryDto inventoryDto =
                textbookStockService.retrieveInventoryByTextbookSn(textbookSn);

        // 입출고 내역 조회 (페이징 + 검색)
        PaginationInfo<TextbookHistoryDto> paginationInfo = new PaginationInfo<>(size, 5, page);
        TextbookHistoryDto condition = TextbookHistoryDto.builder().textbookSn(textbookSn)
                .keyword(keyword).ioTypeCd(ioTypeCd).startDt(startDt).endDt(endDt).build();
        paginationInfo.setDetailCondition(condition);

        int totalCount = textbookStockService.retrieveHistoryListCount(paginationInfo);
        List<TextbookHistoryDto> historyList =
                textbookStockService.retrieveHistoryList(paginationInfo);

        model.addAttribute("inventoryDto", inventoryDto);
        model.addAttribute("historyList", historyList);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("paginationInfo", paginationInfo);
        model.addAttribute("textbookSn", textbookSn);

        return "admin:/textbook/textbook_stock";
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
