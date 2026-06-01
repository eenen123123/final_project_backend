package kr.or.ddit.controller;

import org.springframework.security.core.Authentication;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.or.ddit.finalProject.dto.board.FaqDto;
import kr.or.ddit.finalProject.dto.board.NoticeDto;
import kr.or.ddit.finalProject.dto.board.QnaDto;
import kr.or.ddit.finalProject.service.board.faq.FaqService;
import kr.or.ddit.finalProject.service.board.notice.NoticeService;
import kr.or.ddit.finalProject.service.board.qna.QnaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin/board")
@RequiredArgsConstructor
public class CustomerServiceController {

    private final FaqService faqService;
    private final NoticeService noticeService;
    private final QnaService qnaService;

    // 고객센터 관리 메인 (탭 포함)
    @GetMapping("/customer-service")
    public String customerService(Model model) {
        model.addAttribute("pageTitle", "고객센터 관리 | HERMES");
        model.addAttribute("faqList", faqService.getFaqList(null, null));
        model.addAttribute("noticeList", noticeService.getNoticeList(null));
        model.addAttribute("qnaList", qnaService.getQnaList(null, null));
        return "admin:/board/customer_service";
    }

    // ── FAQ ──────────────────────────────
    @GetMapping("/faq/write")
    public String faqWriteForm(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "FAQ 등록 | HERMES");
        model.addAttribute("currentUser", authentication.getName());
        return "admin:/board/faq/faq_write";
    }

    @PostMapping("/faq/write")
    public String faqWrite(FaqDto faqDto) {
        faqService.createFaq(faqDto);
        return "redirect:/admin/board/customer-service?tab=tab-faq";
    }

    @GetMapping("/faq/edit/{postSn}")
    public String faqEditForm(@PathVariable Long postSn, Model model) {
        model.addAttribute("pageTitle", "FAQ 수정 | HERMES");
        model.addAttribute("faq", faqService.getFaqById(postSn));
        return "admin:/board/faq/faq_edit";
    }

    @PostMapping("/faq/edit/{postSn}")
    public String faqEdit(@PathVariable Long postSn, FaqDto faqDto) {
        faqDto.setPostSn(postSn);
        faqService.updateFaq(faqDto);
        return "redirect:/admin/board/customer-service?tab=tab-faq";
    }

    @PostMapping("/faq/delete/{postSn}")
    public String faqDelete(@PathVariable Long postSn) {
        faqService.deleteFaq(postSn);
        return "redirect:/admin/board/customer-service?tab=tab-faq";
    }

    // ── 공지사항 ─────────────────────────
    @GetMapping("/notice/write")
    public String noticeWriteForm(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "공지사항 등록 | HERMES");
        model.addAttribute("currentUser", authentication.getName());
        return "admin:/board/notice/notice_write";
    }

    @PostMapping("/notice/write")
    public String noticeWrite(NoticeDto noticeDto) {
        noticeService.createNotice(noticeDto);
        return "redirect:/admin/board/customer-service?tab=tab-notice";
    }

    @GetMapping("/notice/edit/{postSn}")
    public String noticeEditForm(@PathVariable Long postSn, Model model) {
        model.addAttribute("pageTitle", "공지사항 수정 | HERMES");
        model.addAttribute("notice", noticeService.getNoticeById(postSn));
        return "admin:/board/notice/notice_edit";
    }

    @PostMapping("/notice/edit/{postSn}")
    public String noticeEdit(@PathVariable Long postSn, NoticeDto noticeDto) {
        noticeDto.setPostSn(postSn);
        noticeService.updateNotice(noticeDto);
        return "redirect:/admin/board/customer-service?tab=tab-notice";
    }

    @PostMapping("/notice/delete/{postSn}")
    public String noticeDelete(@PathVariable Long postSn) {
        noticeService.deleteNotice(postSn);
        return "redirect:/admin/board/customer-service?tab=tab-notice";
    }

    // ── QnA ──────────────────────────────
    @GetMapping("/qna/{postSn}")
    public String qnaDetail(@PathVariable Long postSn, Model model) {
        model.addAttribute("pageTitle", "QnA 답변 | HERMES");
        model.addAttribute("qna", qnaService.getQnaById(postSn));
        return "admin:/board/qna/qna_detail";
    }

    @PostMapping("/qna/{postSn}/answer")
    public String qnaAnswer(@PathVariable Long postSn, QnaDto qnaDto,
            Authentication authentication) {
        qnaDto.setPostSn(postSn);
        qnaDto.setAnswrUserId(authentication.getName());
        qnaService.answerQna(qnaDto);
        return "redirect:/admin/board/customer-service?tab=tab-qna";
    }

    @GetMapping("/qna/edit/{postSn}")
    public String qnaEditForm(@PathVariable Long postSn, Model model) {
        model.addAttribute("pageTitle", "QnA 답변 수정 | HERMES");
        model.addAttribute("qna", qnaService.getQnaById(postSn));
        return "admin:/board/qna/qna_edit";
    }

    @PostMapping("/qna/edit/{postSn}")
    public String qnaEdit(@PathVariable Long postSn, QnaDto qnaDto, Authentication authentication) {
        qnaDto.setPostSn(postSn);
        qnaDto.setAnswrUserId(authentication.getName());
        qnaService.answerQna(qnaDto); // 기존 메서드 재사용
        return "redirect:/admin/board/customer-service?tab=tab-qna";
    }

    @PostMapping("/qna/delete/{postSn}")
    public String qnaDelete(@PathVariable Long postSn) {
        qnaService.deleteQna(postSn);
        return "redirect:/admin/board/customer-service?tab=tab-qna";
    }
}
