package kr.or.ddit.controller.instructor;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import kr.or.ddit.finalProject.dto.common.CommonCodeDto;
import kr.or.ddit.finalProject.dto.file.FileCtxType;
import kr.or.ddit.finalProject.dto.instructor.board.BoardType;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardDto;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardResponse;
import kr.or.ddit.finalProject.service.file.FileUploadService;
import kr.or.ddit.finalProject.service.instructor.InstructorBoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/instructor/board")
@RequiredArgsConstructor
/**
 * 강사 게시판 컨트롤러, 강사 게시판 목록 조회, 상세 조회, 등록, 수정, 삭제(소프트), 복구 기능을 제공
 */
public class InstructorBoardController {

    private final InstructorBoardService instructorBoardService;
    private final FileUploadService fileUploadService;

    /**
     * 강사 게시판 목록 조회
     *
     * @param model
     * @return
     */
    private static final int PAGE_SIZE = 10;

    @GetMapping("/list")
    public String getBoardList(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String boardTypeCd,
            @RequestParam(defaultValue = "") String source,
            @RequestParam(defaultValue = "1") int page,
            Model model) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (page < 1) page = 1;
        var result = instructorBoardService.getInstructorBoardList(userId, keyword, boardTypeCd, source, page, PAGE_SIZE);
        int totalPages = (int) Math.ceil((double) result.getTotalCount() / PAGE_SIZE);
        model.addAttribute("boardList", result.getItems());
        model.addAttribute("totalCount", result.getTotalCount());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", Math.max(totalPages, 1));
        model.addAttribute("keyword", keyword);
        model.addAttribute("boardTypeCd", boardTypeCd);
        model.addAttribute("source", source);
        model.addAttribute("boardTypes", getBoardTypes());
        return "admin:/instructor/board/list";
    }

    /**
     * 게시판 유형 조회 (AJAX)
     *
     * @return
     */
    @GetMapping("/boardTypes")
    @ResponseBody
    public List<CommonCodeDto> getBoardTypes() {
        return Arrays.stream(BoardType.values())
                .map(t -> { CommonCodeDto dto = new CommonCodeDto(); dto.setComCd(t.name()); dto.setComCdNm(t.getLabel()); return dto; })
                .toList();
    }

    /**
     * 강사 게시판 상세 조회
     *
     * @param postSn
     * @param model
     * @return
     */
    @GetMapping("/detail/{postSn}")
    public String getBoardDetail(@PathVariable Long postSn,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String boardTypeCd,
            @RequestParam(defaultValue = "") String source,
            Model model) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        InstructorBoardResponse board =
                instructorBoardService.getInstructorBoardDetail(postSn, userId);
        if (board == null) {
            return "redirect:/instructor/board/list";
        }
        board.setContent(sanitize(board.getContent()));
        model.addAttribute("board", board);
        model.addAttribute("listPage", page);
        model.addAttribute("listKeyword", keyword);
        model.addAttribute("listBoardTypeCd", boardTypeCd);
        model.addAttribute("listSource", source);
        return "admin:/instructor/board/detail";
    }

    /**
     * 강사 게시판 등록 폼 조회
     *
     * @return
     */
    @GetMapping("/insertForm")
    public String getInsertForm(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String boardTypeCd,
            @RequestParam(defaultValue = "") String source,
            Model model) {
        model.addAttribute("boardTypes", getBoardTypes());
        model.addAttribute("listPage", page);
        model.addAttribute("listKeyword", keyword);
        model.addAttribute("listBoardTypeCd", boardTypeCd);
        model.addAttribute("listSource", source);
        return "admin:/instructor/board/insertForm";
    }

    /**
     * 강사 게시판 등록
     *
     * @param instructorBoardDto
     * @param error
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/insert")
    public String insertBoard(@Validated @ModelAttribute InstructorBoardDto instructorBoardDto,
            BindingResult error,
            @RequestParam(value = "attachFiles", required = false) List<MultipartFile> attachFiles,
            @RequestParam(defaultValue = "1") int listPage,
            @RequestParam(defaultValue = "") String listKeyword,
            @RequestParam(defaultValue = "") String listBoardTypeCd,
            @RequestParam(defaultValue = "") String listSource,
            RedirectAttributes redirectAttributes) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        instructorBoardDto.setInstrUserId(userId);
        instructorBoardDto.setWrtrUserId(userId);
        instructorBoardDto.setPostCn(sanitize(instructorBoardDto.getPostCn()));

        if (error.hasErrors()) {
            String errorMsg = error.getAllErrors().stream().map(e -> e.getDefaultMessage())
                    .findFirst().orElse("입력값을 확인해주세요.");
            redirectAttributes.addFlashAttribute("board", instructorBoardDto);
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:" + insertFormUrl(listPage, listKeyword, listBoardTypeCd, listSource);
        }

        if (isBlankHtml(instructorBoardDto.getPostCn())) {
            redirectAttributes.addFlashAttribute("board", instructorBoardDto);
            redirectAttributes.addFlashAttribute("errorMessage", "내용을 입력해주세요.");
            return "redirect:" + insertFormUrl(listPage, listKeyword, listBoardTypeCd, listSource);
        }

        if (hasFiles(attachFiles)) {
            int groupId = fileUploadService.createFileGroup();
            instructorBoardDto.setAtchFileId((long) groupId);
            for (MultipartFile f : attachFiles) {
                if (!f.isEmpty()) {
                    fileUploadService.uploadFile(f, userId, groupId, FileCtxType.INSTRUCTOR, String.valueOf(groupId));
                }
            }
        }
        try {
            int rowcnt = instructorBoardService.insertInstructorBoard(instructorBoardDto);
            if (rowcnt > 0) {
                return "redirect:" + detailUrl(instructorBoardDto.getPostSn(), listPage, listKeyword, listBoardTypeCd, listSource);
            } else {
                redirectAttributes.addFlashAttribute("board", instructorBoardDto);
                redirectAttributes.addFlashAttribute("errorMessage", "게시글 등록에 실패했습니다. 다시 시도해주세요.");
                return "redirect:" + insertFormUrl(listPage, listKeyword, listBoardTypeCd, listSource);
            }
        } catch (Exception e) {
            log.error("게시글 등록 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("board", instructorBoardDto);
            redirectAttributes.addFlashAttribute("errorMessage", "게시글 등록 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "redirect:" + insertFormUrl(listPage, listKeyword, listBoardTypeCd, listSource);
        }
    }

    /**
     * 강사 게시판 수정 폼 조회
     *
     * @param postSn
     * @param model
     * @return
     */
    @GetMapping("/updateForm/{postSn}")
    public String getUpdateForm(@PathVariable Long postSn,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String boardTypeCd,
            @RequestParam(defaultValue = "") String source,
            Model model) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        InstructorBoardResponse responseDto =
                instructorBoardService.getInstructorBoardDetail(postSn, userId);
        if (responseDto == null) {
            return "redirect:/instructor/board/list";
        }
        InstructorBoardDto board = InstructorBoardDto.builder().postSn(responseDto.getPostSn())
                .boardTypeCd(responseDto.getBoardTypeCd()).postSj(responseDto.getTitle())
                .postCn(sanitize(responseDto.getContent())).build();
        model.addAttribute("board", board);
        model.addAttribute("boardTypes", getBoardTypes());
        model.addAttribute("listPage", page);
        model.addAttribute("listKeyword", keyword);
        model.addAttribute("listBoardTypeCd", boardTypeCd);
        model.addAttribute("listSource", source);
        String atchFileIdStr = responseDto.getAtchFileId();
        if (atchFileIdStr != null && !atchFileIdStr.isBlank()) {
            try {
                model.addAttribute("existingFiles",
                        fileUploadService.retrieveFilesByGroupId(Integer.parseInt(atchFileIdStr)));
            } catch (NumberFormatException ignored) {
                // atchFileId가 숫자가 아닌 경우 파일 목록 없이 진행
            }
        }
        return "admin:/instructor/board/insertForm";
    }

    /**
     * 강사 게시판 수정
     *
     * @param instructorBoardDto
     * @param error
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/update")
    public String updateBoard(@Validated @ModelAttribute InstructorBoardDto instructorBoardDto,
            BindingResult error,
            @RequestParam(value = "attachFiles", required = false) List<MultipartFile> attachFiles,
            @RequestParam(defaultValue = "1") int listPage,
            @RequestParam(defaultValue = "") String listKeyword,
            @RequestParam(defaultValue = "") String listBoardTypeCd,
            @RequestParam(defaultValue = "") String listSource,
            RedirectAttributes redirectAttributes) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        instructorBoardDto.setInstrUserId(userId);
        instructorBoardDto.setLastMdfrId(userId);
        instructorBoardDto.setPostCn(sanitize(instructorBoardDto.getPostCn()));

        if (error.hasErrors()) {
            String errorMsg = error.getAllErrors().stream().map(e -> e.getDefaultMessage())
                    .findFirst().orElse("입력값을 확인해주세요.");
            redirectAttributes.addFlashAttribute("board", instructorBoardDto);
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            redirectAttributes.addFlashAttribute("listPage", listPage);
            redirectAttributes.addFlashAttribute("listKeyword", listKeyword);
            redirectAttributes.addFlashAttribute("listBoardTypeCd", listBoardTypeCd);
            redirectAttributes.addFlashAttribute("listSource", listSource);
            return "redirect:" + updateFormUrl(instructorBoardDto.getPostSn(), listPage, listKeyword, listBoardTypeCd, listSource);
        }

        if (isBlankHtml(instructorBoardDto.getPostCn())) {
            redirectAttributes.addFlashAttribute("board", instructorBoardDto);
            redirectAttributes.addFlashAttribute("errorMessage", "내용을 입력해주세요.");
            redirectAttributes.addFlashAttribute("listPage", listPage);
            redirectAttributes.addFlashAttribute("listKeyword", listKeyword);
            redirectAttributes.addFlashAttribute("listBoardTypeCd", listBoardTypeCd);
            redirectAttributes.addFlashAttribute("listSource", listSource);
            return "redirect:" + updateFormUrl(instructorBoardDto.getPostSn(), listPage, listKeyword, listBoardTypeCd, listSource);
        }

        if (hasFiles(attachFiles)) {
            Long existingAtchFileId = instructorBoardDto.getAtchFileId();
            int groupId = existingAtchFileId != null
                    ? existingAtchFileId.intValue()
                    : fileUploadService.createFileGroup();
            instructorBoardDto.setAtchFileId((long) groupId);
            for (MultipartFile f : attachFiles) {
                if (!f.isEmpty()) {
                    fileUploadService.uploadFile(f, userId, groupId, FileCtxType.INSTRUCTOR, String.valueOf(groupId));
                }
            }
        }

        try {
            int rowcnt = instructorBoardService.updateInstructorBoard(instructorBoardDto);
            if (rowcnt > 0) {
                return "redirect:" + detailUrl(instructorBoardDto.getPostSn(), listPage, listKeyword, listBoardTypeCd, listSource);
            } else {
                redirectAttributes.addFlashAttribute("board", instructorBoardDto);
                redirectAttributes.addFlashAttribute("errorMessage", "게시글 수정에 실패했습니다. 다시 시도해주세요.");
                return "redirect:" + updateFormUrl(instructorBoardDto.getPostSn(), listPage, listKeyword, listBoardTypeCd, listSource);
            }
        } catch (Exception e) {
            log.error("게시글 수정 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("board", instructorBoardDto);
            redirectAttributes.addFlashAttribute("errorMessage", "게시글 수정 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "redirect:" + updateFormUrl(instructorBoardDto.getPostSn(), listPage, listKeyword, listBoardTypeCd, listSource);
        }
    }

    /**
     * 강사 게시판 삭제 (소프트)
     */
    @PostMapping("/delete/{postSn}")
    public String deleteBoard(@PathVariable Long postSn,
            @RequestParam(defaultValue = "1") int listPage,
            @RequestParam(defaultValue = "") String listKeyword,
            @RequestParam(defaultValue = "") String listBoardTypeCd,
            @RequestParam(defaultValue = "") String listSource,
            RedirectAttributes redirectAttributes) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        instructorBoardService.deleteInstructorBoard(postSn, userId);
        redirectAttributes.addFlashAttribute("successMessage", "게시글이 삭제되었습니다.");
        return "redirect:" + detailUrl(postSn, listPage, listKeyword, listBoardTypeCd, listSource);
    }

    /**
     * 강사 게시판 Q&A 답변 등록
     */
    @PostMapping("/answer/{postSn}")
    public String answerBoard(@PathVariable Long postSn,
            @RequestParam String answCn,
            @RequestParam(defaultValue = "1") int listPage,
            @RequestParam(defaultValue = "") String listKeyword,
            @RequestParam(defaultValue = "") String listBoardTypeCd,
            @RequestParam(defaultValue = "") String listSource,
            RedirectAttributes redirectAttributes) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        int rows = instructorBoardService.answerInstructorQna(postSn, userId, answCn);
        if (rows == 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Q&A 게시글에만 답변을 등록할 수 있습니다.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "답변이 등록되었습니다.");
        }
        return "redirect:" + detailUrl(postSn, listPage, listKeyword, listBoardTypeCd, listSource);
    }

    /**
     * 강사 게시판 복구
     *
     * @param postSn
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/restore/{postSn}")
    public String restoreBoard(@PathVariable Long postSn,
            @RequestParam(defaultValue = "1") int listPage,
            @RequestParam(defaultValue = "") String listKeyword,
            @RequestParam(defaultValue = "") String listBoardTypeCd,
            @RequestParam(defaultValue = "") String listSource,
            RedirectAttributes redirectAttributes) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        instructorBoardService.restoreInstructorBoard(postSn, userId);
        redirectAttributes.addFlashAttribute("successMessage", "게시글이 복구되었습니다.");
        return "redirect:" + detailUrl(postSn, listPage, listKeyword, listBoardTypeCd, listSource);
    }

    private String detailUrl(Long postSn, int page, String keyword, String boardTypeCd, String source) {
        return UriComponentsBuilder.fromPath("/instructor/board/detail/{postSn}")
                .queryParam("page", page)
                .queryParam("keyword", keyword)
                .queryParam("boardTypeCd", boardTypeCd)
                .queryParam("source", source)
                .buildAndExpand(postSn)
                .toUriString();
    }

    private String insertFormUrl(int page, String keyword, String boardTypeCd, String source) {
        return UriComponentsBuilder.fromPath("/instructor/board/insertForm")
                .queryParam("page", page)
                .queryParam("keyword", keyword)
                .queryParam("boardTypeCd", boardTypeCd)
                .queryParam("source", source)
                .toUriString();
    }

    private String updateFormUrl(Long postSn, int page, String keyword, String boardTypeCd, String source) {
        return UriComponentsBuilder.fromPath("/instructor/board/updateForm/{postSn}")
                .queryParam("page", page)
                .queryParam("keyword", keyword)
                .queryParam("boardTypeCd", boardTypeCd)
                .queryParam("source", source)
                .buildAndExpand(postSn)
                .toUriString();
    }

    private boolean hasFiles(List<MultipartFile> files) {
        return files != null && !files.isEmpty() && !files.get(0).isEmpty();
    }

    // Tiptap이 빈 상태에서 제출해도 <p></p> 같은 HTML을 보내므로 텍스트 노드 유무로 판별
    private boolean isBlankHtml(String html) {
        if (html == null) return true;
        return Jsoup.parse(html).text().isBlank();
    }

    // Toast UI Editor 허용 태그 목록 — 매 호출마다 재생성하지 않도록 static 상수로 선언
    private static final Safelist TOAST_SAFELIST = Safelist.relaxed().preserveRelativeLinks(true)
            .addTags("del", "s", "hr", "input")
            .addAttributes("input", "type", "checked", "disabled").addAttributes("span", "style")
            .addAttributes("p", "style").addAttributes("h1", "style").addAttributes("h2", "style")
            .addAttributes("h3", "style").addAttributes("h4", "style").addAttributes("h5", "style")
            .addAttributes("h6", "style").addAttributes("img", "src")
            .addProtocols("img", "src", "http", "https", "data");

    // XSS 방어: 허용 태그만 남기고 나머지 제거
    private String sanitize(String html) {
        if (html == null)
            return null;
        return Jsoup.clean(html, TOAST_SAFELIST);
    }

}
