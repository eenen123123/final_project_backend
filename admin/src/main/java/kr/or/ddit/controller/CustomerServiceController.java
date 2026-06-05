package kr.or.ddit.controller;

import java.io.IOException;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
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
import org.springframework.web.multipart.MultipartFile;

import kr.or.ddit.finalProject.dto.board.DataRoomDto;
import kr.or.ddit.finalProject.dto.board.FaqDto;
import kr.or.ddit.finalProject.dto.board.NoticeDto;
import kr.or.ddit.finalProject.dto.board.QnaDto;
import kr.or.ddit.finalProject.dto.board.req.DataRoomSearchCondition;
import kr.or.ddit.finalProject.dto.board.req.FaqSearchCondition;
import kr.or.ddit.finalProject.dto.board.req.NoticeSearchCondition;
import kr.or.ddit.finalProject.dto.board.req.QnaSearchCondition;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.service.board.dataroom.DataRoomService;
import kr.or.ddit.finalProject.service.board.faq.FaqService;
import kr.or.ddit.finalProject.service.board.notice.NoticeService;
import kr.or.ddit.finalProject.service.board.qna.QnaService;
import kr.or.ddit.finalProject.service.file.CloudinaryUploadService;
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
    private final CloudinaryUploadService cloudinaryUploadService;

    private static final int ADMIN_ALL = 500;

    @GetMapping("/customer-service")
    public String customerService(Model model) {
        model.addAttribute("pageTitle", "고객센터 관리 | HERMES");
        model.addAttribute("faqList",
                faqService.getList(allOf(new FaqSearchCondition())).getItems());
        model.addAttribute("noticeList",
                noticeService.getList(allOf(new NoticeSearchCondition())).getItems());
        model.addAttribute("qnaList",
                qnaService.getList(allOf(new QnaSearchCondition())).getItems());
        model.addAttribute("dataRoomList",
                dataRoomService.getList(allOf(new DataRoomSearchCondition())).getItems());
        return "admin:/board/customer_service";
    }

    // ── 에디터 이미지 업로드 ──────────────────────────────
    @ResponseBody
    @PostMapping("/upload/editor-image")
    public ResponseEntity<Map<String, String>> uploadEditorImage(@RequestParam MultipartFile image)
            throws IOException {
        String url = cloudinaryUploadService.uploadFileToCloudinary(image);
        return ResponseEntity.ok(Map.of("url", url));
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
            @RequestParam(required = false) MultipartFile attachFile,
            Authentication authentication) {
        dataRoomDto.setAttachFile(attachFile);
        dataRoomService.create(dataRoomDto, authentication);
        return "redirect:/admin/board/customer-service?tab=tab-dataroom";
    }

    @GetMapping("/dataroom/edit/{postSn}")
    public String dataRoomEditForm(@PathVariable Long postSn, Model model) {
        model.addAttribute("pageTitle", "자료실 수정 | HERMES");
        DataRoomDto dataRoom = dataRoomService.getById(postSn, null);
        dataRoom.setPostCn(sanitize(dataRoom.getPostCn()));
        model.addAttribute("dataRoom", dataRoom);
        return "admin:/board/dataroom/dataroom_edit";
    }

    @PostMapping("/dataroom/edit/{postSn}")
    public String dataRoomEdit(@PathVariable Long postSn, DataRoomDto dataRoomDto,
            @RequestParam(required = false) MultipartFile attachFile,
            Authentication authentication) {
        dataRoomDto.setPostSn(postSn);
        dataRoomDto.setPostCn(sanitize(dataRoomDto.getPostCn()));
        dataRoomDto.setAttachFile(attachFile);
        dataRoomService.update(dataRoomDto);
        return "redirect:/admin/board/customer-service?tab=tab-dataroom";
    }

    @PostMapping("/dataroom/delete/{postSn}")
    public String dataRoomDelete(@PathVariable Long postSn) {
        dataRoomService.delete(postSn);
        return "redirect:/admin/board/customer-service?tab=tab-dataroom";
    }

    private <C> PaginationInfo<C> allOf(C condition) {
        PaginationInfo<C> info = new PaginationInfo<>(ADMIN_ALL, 1, 1);
        info.setDetailCondition(condition);
        return info;
    }

    private static final Safelist TOAST_SAFELIST = Safelist.relaxed()
            .addTags("del", "s", "hr", "input")
            .addAttributes("input", "type", "checked", "disabled").addAttributes("span", "style")
            .addAttributes("p", "style").addAttributes("h1", "style").addAttributes("h2", "style")
            .addAttributes("h3", "style").addAttributes("h4", "style").addAttributes("h5", "style")
            .addAttributes("h6", "style").addAttributes("img", "src");

    private String sanitize(String html) {
        if (html == null)
            return null;
        return Jsoup.clean(html, TOAST_SAFELIST);
    }
}
