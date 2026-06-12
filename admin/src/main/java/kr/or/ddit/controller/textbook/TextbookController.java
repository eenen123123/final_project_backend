package kr.or.ddit.controller.textbook;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.web.bind.annotation.ResponseBody;

import kr.or.ddit.finalProject.dto.course.AdminCourseSearchCondition;
import kr.or.ddit.finalProject.dto.subject.SubjectDto;
import kr.or.ddit.finalProject.dto.textbook.TextbookDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.service.course.CourseService;
import kr.or.ddit.finalProject.service.file.CloudinaryUploadService;
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
    private final CloudinaryUploadService cloudinaryUploadService; // 이미지 업로드 용

    // 대분류별 소분류 목록 조회 (AJAX)
    @GetMapping("/api/subjects")
    @ResponseBody
    public List<SubjectDto> subjectsBySubjClId(@RequestParam Long subjClId) {
        return courseService.retrieveSubjectsBySubjClId(subjClId);
    }

    // 강좌 선택 팝업
    @GetMapping("/popup/courses")
    public String coursePopup(Model model) {
        model.addAttribute("courseList", courseService.retrieveCourseList(new PaginationInfo<AdminCourseSearchCondition>(1000, 1)));
        model.addAttribute("subjClList", courseService.retrieveSubjectClassificationList());
        return "textbook/textbookPopup";
    }

    // 교재 목록
    @GetMapping
    public String textbookList(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long subjClId,
            @RequestParam(defaultValue = "false") boolean archived,
            @RequestParam(defaultValue = "recent") String sort,
            Model model) {
        PaginationInfo<TextbookDto> paginationInfo = new PaginationInfo<>(size, 5, page);
        TextbookDto condition = TextbookDto.builder()
                .keyword(keyword).subjClId(subjClId).showArchived(archived).sort(sort).build();
        paginationInfo.setDetailCondition(condition);
        int totalCount = textbookService.retrieveTextbookListCount(paginationInfo);
        List<TextbookDto> textbookList = textbookService.retrieveTextbookList(paginationInfo);
        model.addAttribute("textbookList", textbookList);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("paginationInfo", paginationInfo);
        model.addAttribute("subjClList", courseService.retrieveSubjectClassificationList());
        model.addAttribute("newCount", textbookService.retrieveNewTextbookCountThisMonth());
        model.addAttribute("dangerCount", textbookService.retrieveDangerTextbookCount());
        model.addAttribute("archivedCount", textbookService.retrieveArchivedTextbookCount());
        model.addAttribute("archived", archived);

        return "admin:/textbook/textbook_list";
    }

    // 교재 상세페이지
    @GetMapping("/{textbookSn}/detail")
    public String textbookDetail(@PathVariable Long textbookSn, Model model) {
        TextbookDto textbookDto = textbookService.retrieveTextbookBySn(textbookSn);
        model.addAttribute("textbookDto", textbookDto);
        return "admin:/textbook/textbook_detail";
    }

    // 교재 등록 폼
    @GetMapping("/new")
    public String textbookNewForm(Model model) {
        model.addAttribute("textbookDto", new TextbookDto());
        model.addAttribute("subjClList", courseService.retrieveSubjectClassificationList());
        model.addAttribute("courseList", courseService.retrieveCourseList(new PaginationInfo<AdminCourseSearchCondition>(1000, 1)));
        return "admin:/textbook/textbook_form";
    }

    // 교재 등록 처리
    @PostMapping("/new")
    public String textbookCreate(TextbookDto textbookDto,
            @RequestParam(defaultValue = "0") int initInvtCnt,
            @RequestParam(value = "thmbImgFile", required = false) MultipartFile thmbImgFile,
            Authentication authentication, RedirectAttributes redirectAttributes)
            throws IOException {

        String userId = authentication.getName();
        textbookDto.setRgtrId(userId);
        textbookDto.setLastMdfrId(userId);

        // 이미지 업로드 처리
        if (thmbImgFile != null && !thmbImgFile.isEmpty()) {
            String imgUrl = cloudinaryUploadService.uploadFileToCloudinary(thmbImgFile);
            textbookDto.setThmbImg(imgUrl);
        }

        textbookService.createTextbook(textbookDto, initInvtCnt);
        redirectAttributes.addFlashAttribute("successMsg", "교재가 등록되었습니다.");
        return "redirect:/admin/textbook";
    }

    // 교재 상세/수정 폼
    @GetMapping("/{textbookSn}")
    public String textbookEditForm(@PathVariable Long textbookSn, Model model) {
        TextbookDto textbookDto = textbookService.retrieveTextbookBySn(textbookSn);
        model.addAttribute("textbookDto", textbookDto);
        model.addAttribute("subjClList", courseService.retrieveSubjectClassificationList());
        model.addAttribute("courseList", courseService.retrieveCourseList(new PaginationInfo<AdminCourseSearchCondition>(1000, 1)));
        return "admin:/textbook/textbook_form";
    }

    // 교재 수정 처리
    @PostMapping("/{textbookSn}")
    public String textbookUpdate(@PathVariable Long textbookSn, TextbookDto textbookDto,
            @RequestParam(value = "thmbImgFile", required = false) MultipartFile thmbImgFile,
            @RequestParam(value = "isImgDeleted", defaultValue = "N") String isImgDeleted,
            Authentication authentication, RedirectAttributes redirectAttributes)
            throws IOException {

        textbookDto.setTextbookSn(textbookSn);

        // 이미지 삭제 처리
        if ("Y".equals(isImgDeleted)) {
            textbookDto.setThmbImg(null);
        } // 새 이미지 업로드
        else if (thmbImgFile != null && !thmbImgFile.isEmpty()) {
            String imgUrl = cloudinaryUploadService.uploadFileToCloudinary(thmbImgFile);
            textbookDto.setThmbImg(imgUrl);
        } else {
            // 이미지 변경 없으면 기존 이미지 유지
            TextbookDto original = textbookService.retrieveTextbookBySn(textbookSn);
            textbookDto.setThmbImg(original.getThmbImg());
        }

        textbookService.modifyTextbook(textbookDto, authentication.getName());
        redirectAttributes.addFlashAttribute("successMsg", "교재 정보가 수정되었습니다.");
        return "redirect:/admin/textbook";
    }

    // 교재 삭제
    @PostMapping("/{textbookSn}/delete")
    public String textbookDelete(@PathVariable Long textbookSn, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        textbookService.removeTextbook(textbookSn, authentication.getName());
        redirectAttributes.addFlashAttribute("successMsg", "교재 정보가 삭제되었습니다.");
        return "redirect:/admin/textbook";
    }
}
