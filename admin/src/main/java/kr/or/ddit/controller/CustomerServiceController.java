package kr.or.ddit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

import kr.or.ddit.finalProject.dto.board.DataRoomDto;
import kr.or.ddit.finalProject.util.TipTapSanitizer;
import kr.or.ddit.finalProject.dto.board.FaqDto;
import kr.or.ddit.finalProject.dto.board.NoticeDto;
import kr.or.ddit.finalProject.dto.board.QnaDto;
import kr.or.ddit.finalProject.dto.board.req.DataRoomSearchCondition;
import kr.or.ddit.finalProject.dto.board.req.FaqSearchCondition;
import kr.or.ddit.finalProject.dto.board.req.NoticeSearchCondition;
import kr.or.ddit.finalProject.dto.board.req.QnaSearchCondition;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.service.board.dataroom.DataRoomService;
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
    private final DataRoomService dataRoomService;

    @GetMapping("/customer-service")
    public String customerService(Model model) {
        model.addAttribute("pageTitle", "고객센터 관리 | HERMES");
        return "admin:/board/customer_service";
    }

    // ── 비동기 목록 API (JSON) ────────────────────────────
    @ResponseBody
    @GetMapping("/faq/paged")
    public ResponseEntity<PageResponse<FaqDto>> faqPaged(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String faqCtgCd) {
        PaginationInfo<FaqSearchCondition> pi = new PaginationInfo<>(size, 5, page);
        pi.setDetailCondition(new FaqSearchCondition(keyword, faqCtgCd, null));
        return ResponseEntity.ok(faqService.getList(pi));
    }

    @ResponseBody
    @GetMapping("/notice/paged")
    public ResponseEntity<PageResponse<NoticeDto>> noticePaged(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String noticeTypeCd) {
        PaginationInfo<NoticeSearchCondition> pi = new PaginationInfo<>(size, 5, page);
        pi.setDetailCondition(new NoticeSearchCondition(keyword, noticeTypeCd));
        return ResponseEntity.ok(noticeService.getList(pi));
    }

    @ResponseBody
    @GetMapping("/qna/paged")
    public ResponseEntity<PageResponse<QnaDto>> qnaPaged(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String qnaCtgCd,
            @RequestParam(required = false) String answStatCd) {
        PaginationInfo<QnaSearchCondition> pi = new PaginationInfo<>(size, 5, page);
        pi.setDetailCondition(new QnaSearchCondition(keyword, qnaCtgCd, answStatCd, null));
        return ResponseEntity.ok(qnaService.getList(pi));
    }

    @ResponseBody
    @GetMapping("/dataroom/paged")
    public ResponseEntity<PageResponse<DataRoomDto>> dataRoomPaged(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String dataCtg) {
        PaginationInfo<DataRoomSearchCondition> pi = new PaginationInfo<>(size, 5, page);
        pi.setDetailCondition(new DataRoomSearchCondition(keyword, dataCtg));
        return ResponseEntity.ok(dataRoomService.getList(pi));
    }

    // ── FAQ 상세 ─────────────────────────────────────────
    @GetMapping("/faq/{postSn}")
    public String faqDetail(@PathVariable Long postSn, Model model) {
        model.addAttribute("pageTitle", "FAQ 상세 | HERMES");
        model.addAttribute("faq", faqService.getById(postSn, null));
        return "admin:/board/faq/faq_detail";
    }

    // ── 공지사항 상세 ─────────────────────────────────────
    @GetMapping("/notice/{postSn}")
    public String noticeDetail(@PathVariable Long postSn, Model model) {
        model.addAttribute("pageTitle", "공지사항 상세 | HERMES");
        model.addAttribute("notice", noticeService.getById(postSn, null));
        return "admin:/board/notice/notice_detail";
    }

    // ── 자료실 상세 ──────────────────────────────────────
    @GetMapping("/dataroom/{postSn}")
    public String dataRoomDetail(@PathVariable Long postSn, Model model) {
        model.addAttribute("pageTitle", "자료실 상세 | HERMES");
        model.addAttribute("dataRoom", dataRoomService.getById(postSn, null));
        return "admin:/board/dataroom/dataroom_detail";
    }

    // ── FAQ ──────────────────────────────
    @GetMapping("/faq/write")
    public String faqWriteForm(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "FAQ 등록 | HERMES");
        model.addAttribute("currentUser", authentication.getName());
        return "admin:/board/faq/faq_write";
    }

    @PostMapping("/faq/write")
    public String faqWrite(FaqDto faqDto, Authentication authentication) {
        faqService.create(faqDto, authentication);
        return "redirect:/admin/board/customer-service?tab=tab-faq";
    }

    @GetMapping("/faq/edit/{postSn}")
    public String faqEditForm(@PathVariable Long postSn, Model model) {
        model.addAttribute("pageTitle", "FAQ 수정 | HERMES");
        model.addAttribute("faq", faqService.getById(postSn, null));
        return "admin:/board/faq/faq_edit";
    }

    @PostMapping("/faq/edit/{postSn}")
    public String faqEdit(@PathVariable Long postSn, FaqDto faqDto) {
        faqDto.setPostSn(postSn);
        faqService.update(faqDto);
        return "redirect:/admin/board/customer-service?tab=tab-faq";
    }

    @PostMapping("/faq/delete/{postSn}")
    public String faqDelete(@PathVariable Long postSn) {
        faqService.delete(postSn);
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
    public String noticeWrite(NoticeDto noticeDto, Authentication authentication) {
        noticeService.create(noticeDto, authentication);
        return "redirect:/admin/board/customer-service?tab=tab-notice";
    }

    @GetMapping("/notice/edit/{postSn}")
    public String noticeEditForm(@PathVariable Long postSn, Model model) {
        model.addAttribute("pageTitle", "공지사항 수정 | HERMES");
        model.addAttribute("notice", noticeService.getById(postSn, null));
        return "admin:/board/notice/notice_edit";
    }

    @PostMapping("/notice/edit/{postSn}")
    public String noticeEdit(@PathVariable Long postSn, NoticeDto noticeDto) {
        noticeDto.setPostSn(postSn);
        noticeService.update(noticeDto);
        return "redirect:/admin/board/customer-service?tab=tab-notice";
    }

    @PostMapping("/notice/delete/{postSn}")
    public String noticeDelete(@PathVariable Long postSn) {
        noticeService.delete(postSn);
        return "redirect:/admin/board/customer-service?tab=tab-notice";
    }

    // ── QnA ──────────────────────────────
    @GetMapping("/qna/{postSn}")
    public String qnaDetail(@PathVariable Long postSn, Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "QnA 답변 | HERMES");
        model.addAttribute("qna", qnaService.getById(postSn, authentication));
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
    public String qnaEditForm(@PathVariable Long postSn, Model model,
            Authentication authentication) {
        model.addAttribute("pageTitle", "QnA 답변 수정 | HERMES");
        model.addAttribute("qna", qnaService.getById(postSn, authentication));
        return "admin:/board/qna/qna_edit";
    }

    @PostMapping("/qna/edit/{postSn}")
    public String qnaEdit(@PathVariable Long postSn, QnaDto qnaDto, Authentication authentication) {
        qnaDto.setPostSn(postSn);
        qnaDto.setAnswrUserId(authentication.getName());
        qnaService.answerQna(qnaDto);
        return "redirect:/admin/board/customer-service?tab=tab-qna";
    }

    @PostMapping("/qna/delete/{postSn}")
    public String qnaDelete(@PathVariable Long postSn) {
        qnaService.delete(postSn);
        return "redirect:/admin/board/customer-service?tab=tab-qna";
    }

    // ── 자료실 ──────────────────────────────
    @GetMapping("/dataroom/write")
    public String dataRoomWriteForm(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "자료실 등록 | HERMES");
        model.addAttribute("currentUser", authentication.getName());
        return "admin:/board/dataroom/dataroom_write";
    }

    @PostMapping("/dataroom/write")
    public String dataRoomWrite(DataRoomDto dataRoomDto,
            @RequestParam(value = "attachFiles", required = false) List<MultipartFile> attachFiles,
            Authentication authentication) {
        dataRoomDto.setAttachFiles(attachFiles);
        dataRoomService.create(dataRoomDto, authentication);
        return "redirect:/admin/board/customer-service?tab=tab-dataroom";
    }

    @GetMapping("/dataroom/edit/{postSn}")
    public String dataRoomEditForm(@PathVariable Long postSn, Model model) {
        model.addAttribute("pageTitle", "자료실 수정 | HERMES");
        DataRoomDto dataRoom = dataRoomService.getById(postSn, null);
        dataRoom.setPostCn(TipTapSanitizer.clean(dataRoom.getPostCn()));
        model.addAttribute("dataRoom", dataRoom);
        return "admin:/board/dataroom/dataroom_edit";
    }

    @PostMapping("/dataroom/edit/{postSn}")
    public String dataRoomEdit(@PathVariable Long postSn, DataRoomDto dataRoomDto,
            @RequestParam(value = "attachFiles", required = false) List<MultipartFile> attachFiles,
            Authentication authentication) {
        dataRoomDto.setPostSn(postSn);
        dataRoomDto.setPostCn(TipTapSanitizer.clean(dataRoomDto.getPostCn()));
        dataRoomDto.setAttachFiles(attachFiles);
        dataRoomService.update(dataRoomDto);
        return "redirect:/admin/board/customer-service?tab=tab-dataroom";
    }

    @PostMapping("/dataroom/delete/{postSn}")
    public String dataRoomDelete(@PathVariable Long postSn) {
        dataRoomService.delete(postSn);
        return "redirect:/admin/board/customer-service?tab=tab-dataroom";
    }

}
