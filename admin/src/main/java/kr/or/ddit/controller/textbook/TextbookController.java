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
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.service.course.CourseService;
import kr.or.ddit.finalProject.service.textbook.TextbookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin/textbook")
@RequiredArgsConstructor
public class TextbookController {

    private final TextbookService textbookService;
    private final CourseService courseService; // 과목 분류 조회용

    // 교재 목록
    @GetMapping
    public String textbookList(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long subjClId, Model model) {
        PaginationInfo<TextbookDto> paginationInfo = new PaginationInfo<>(size, 5, page);
        TextbookDto condition = TextbookDto.builder().keyword(keyword).subjClId(subjClId).build();
        paginationInfo.setDetailCondition(condition);
        int totalCount = textbookService.retrieveTextbookListCount(paginationInfo);
        List<TextbookDto> textbookList = textbookService.retrieveTextbookList(paginationInfo);
        model.addAttribute("textbookList", textbookList);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("paginationInfo", paginationInfo);
        model.addAttribute("subjClList", courseService.retrieveSubjectClassificationList());
        return "admin:/textbook/textbook_list";
    }

    // 교재 등록 폼
    @GetMapping("/new")
    public String textbookNewForm(Model model) {
        model.addAttribute("textbookDto", new TextbookDto());
        model.addAttribute("subjClList", courseService.retrieveSubjectClassificationList()); // 추가
        return "admin:/textbook/textbook_form";
    }

    // 교재 등록 처리
    @PostMapping("/new")
    public String textbookCreate(TextbookDto textbookDto,
            @RequestParam(defaultValue = "0") int initInvtCnt, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        String userId = authentication.getName();
        textbookDto.setRgtrId(userId);
        textbookDto.setLastMdfrId(userId);
        textbookService.createTextbook(textbookDto, initInvtCnt);
        redirectAttributes.addFlashAttribute("successMsg", "교재가 등록되었습니다.");
        return "redirect:/admin/textbook";
    }

    // 교재 상세/수정 폼
    @GetMapping("/{textbookSn}")
    public String textbookEditForm(@PathVariable Long textbookSn, Model model) {
        TextbookDto textbookDto = textbookService.retrieveTextbookBySn(textbookSn);
        model.addAttribute("textbookDto", textbookDto);
        model.addAttribute("subjClList", courseService.retrieveSubjectClassificationList()); // 추가
        return "admin:/textbook/textbook_form";
    }

    // 교재 수정 처리
    @PostMapping("/{textbookSn}")
    public String textbookUpdate(@PathVariable Long textbookSn, TextbookDto textbookDto,
            Authentication authentication, RedirectAttributes redirectAttributes) {
        textbookDto.setTextbookSn(textbookSn);
        textbookService.modifyTextbook(textbookDto, authentication.getName());
        redirectAttributes.addFlashAttribute("successMsg", "교재가 수정되었습니다.");
        return "redirect:/admin/textbook";
    }

    // 교재 삭제
    @PostMapping("/{textbookSn}/delete")
    public String textbookDelete(@PathVariable Long textbookSn, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        textbookService.removeTextbook(textbookSn, authentication.getName());
        redirectAttributes.addFlashAttribute("successMsg", "교재가 삭제되었습니다.");
        return "redirect:/admin/textbook";
    }
}
