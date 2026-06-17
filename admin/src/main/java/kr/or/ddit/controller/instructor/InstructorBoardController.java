package kr.or.ddit.controller.instructor;

import java.util.Arrays;
import java.util.List;

import org.jsoup.Jsoup;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import kr.or.ddit.finalProject.dto.common.CommonCodeDto;
import kr.or.ddit.finalProject.util.TipTapSanitizer;
import kr.or.ddit.finalProject.dto.file.FileCtxType;
import kr.or.ddit.finalProject.dto.instructor.board.BoardType;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardDto;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardResponse;
import kr.or.ddit.finalProject.service.file.FileUploadService;
import kr.or.ddit.finalProject.service.instructor.InstructorBoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 강사 게시판 컨트롤러, 강사 게시판 목록 조회, 상세 조회, 등록, 수정, 삭제(소프트), 복구 기능을 제공
 */
@Slf4j
@Controller
@RequestMapping("/instructor/board")
@RequiredArgsConstructor
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
            @RequestParam(defaultValue = "1") int page,
            Model model) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (page < 1) {
            page = 1;
        }
        var result = instructorBoardService.getInstructorBoardList(userId, keyword, boardTypeCd, page, PAGE_SIZE);
        int totalPages = (int) Math.ceil((double) result.getTotalCount() / PAGE_SIZE);
        model.addAttribute("boardList", result.getItems());
        model.addAttribute("totalCount", result.getTotalCount());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", Math.max(totalPages, 1));
        model.addAttribute("keyword", keyword);
        model.addAttribute("boardTypeCd", boardTypeCd);
        model.addAttribute("boardTypes", boardTypeList());
        return "admin:/instructor/board/list-instructor-board";
    }

    /**
     * 게시판 유형 조회 (AJAX)
     *
     * @return
     */
    @GetMapping("/boardTypes")
    @ResponseBody
    public List<CommonCodeDto> getBoardTypes() {
        return boardTypeList();
    }

    private static final List<CommonCodeDto> BOARD_TYPE_LIST = Arrays.stream(BoardType.values())
            .map(t -> {
                CommonCodeDto dto = new CommonCodeDto();
                dto.setComCd(t.name());
                dto.setComCdNm(t.getLabel());
                return dto;
            })
            .toList();

    private List<CommonCodeDto> boardTypeList() {
        return BOARD_TYPE_LIST;
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
            Model model) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        InstructorBoardResponse board
                = instructorBoardService.getInstructorBoardDetail(postSn, userId);
        if (board == null) {
            return "redirect:/instructor/board/list";
        }
        model.addAttribute("board", board);
        model.addAttribute("listPage", page);
        model.addAttribute("listKeyword", keyword);
        model.addAttribute("listBoardTypeCd", boardTypeCd);
        return "admin:/instructor/board/detail-instructor-board";
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
            Model model) {
        model.addAttribute("boardTypes", boardTypeList());
        model.addAttribute("listPage", page);
        model.addAttribute("listKeyword", keyword);
        model.addAttribute("listBoardTypeCd", boardTypeCd);
        return "admin:/instructor/board/form-instructor-board";
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
            @RequestParam(required = false) List<MultipartFile> attachFiles,
            @RequestParam(defaultValue = "1") int listPage,
            @RequestParam(defaultValue = "") String listKeyword,
            @RequestParam(defaultValue = "") String listBoardTypeCd,
            RedirectAttributes redirectAttributes) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        instructorBoardDto.setInstrUserId(userId);
        instructorBoardDto.setWrtrUserId(userId);
        instructorBoardDto.setPostCn(TipTapSanitizer.clean(instructorBoardDto.getPostCn()));

        if (error.hasErrors()) {
            String errorMsg = error.getAllErrors().stream().map(e -> e.getDefaultMessage())
                    .findFirst().orElse("입력값을 확인해주세요.");
            redirectAttributes.addFlashAttribute("board", instructorBoardDto);
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:" + insertFormUrl(listPage, listKeyword, listBoardTypeCd);
        }

        if (isBlankHtml(instructorBoardDto.getPostCn())) {
            redirectAttributes.addFlashAttribute("board", instructorBoardDto);
            redirectAttributes.addFlashAttribute("errorMessage", "내용을 입력해주세요.");
            return "redirect:" + insertFormUrl(listPage, listKeyword, listBoardTypeCd);
        }

        if ("QNA".equals(instructorBoardDto.getBoardTypeCd())) {
            redirectAttributes.addFlashAttribute("board", instructorBoardDto);
            redirectAttributes.addFlashAttribute("errorMessage", "Q&A 게시글은 직접 작성할 수 없습니다.");
            return "redirect:" + insertFormUrl(listPage, listKeyword, listBoardTypeCd);
        }

        // 1. DB INSERT 먼저 — 파일 업로드 전 실패하면 고아 파일 없음
        int rowcnt;
        try {
            rowcnt = instructorBoardService.insertInstructorBoard(instructorBoardDto);
        } catch (Exception e) {
            log.error("게시글 등록 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("board", instructorBoardDto);
            redirectAttributes.addFlashAttribute("errorMessage", "게시글 등록 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "redirect:" + insertFormUrl(listPage, listKeyword, listBoardTypeCd);
        }

        if (rowcnt <= 0) {
            redirectAttributes.addFlashAttribute("board", instructorBoardDto);
            redirectAttributes.addFlashAttribute("errorMessage", "게시글 등록에 실패했습니다. 다시 시도해주세요.");
            return "redirect:" + insertFormUrl(listPage, listKeyword, listBoardTypeCd);
        }

        // 2. INSERT 성공 후 파일 업로드 → atchFileId UPDATE
        if (hasFiles(attachFiles)) {
            int groupId = -1;
            try {
                groupId = fileUploadService.createFileGroup();
                for (MultipartFile f : attachFiles) {
                    if (!f.isEmpty()) {
                        fileUploadService.uploadFile(f, userId, groupId, FileCtxType.INSTRUCTOR, String.valueOf(groupId));
                    }
                }
                instructorBoardDto.setAtchFileId((long) groupId);
                instructorBoardDto.setLastMdfrId(userId);
                instructorBoardService.updateInstructorBoard(instructorBoardDto);
                log.info("게시글 등록 및 파일 업로드 성공 (postSn={}, groupId={})", instructorBoardDto.getPostSn(), groupId);
            } catch (Exception e) {
                log.warn("파일 업로드 중 오류 발생 (게시글은 등록됨, groupId={}): {}", groupId, e.getMessage());
                cleanupFileGroup(groupId, userId);
            }
        }

        return "redirect:" + detailUrl(instructorBoardDto.getPostSn(), listPage, listKeyword, listBoardTypeCd);
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
            Model model) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        InstructorBoardResponse responseDto
                = instructorBoardService.getInstructorBoardDetail(postSn, userId);
        if (responseDto == null) {
            return "redirect:/instructor/board/list";
        }
        InstructorBoardDto board = InstructorBoardDto.builder().postSn(responseDto.getPostSn())
                .boardTypeCd(responseDto.getBoardTypeCd()).postSj(responseDto.getTitle())
                .postCn(responseDto.getContent()).build();
        model.addAttribute("board", board);
        model.addAttribute("boardTypes", boardTypeList());
        model.addAttribute("listPage", page);
        model.addAttribute("listKeyword", keyword);
        model.addAttribute("listBoardTypeCd", boardTypeCd);
        String atchFileIdStr = responseDto.getAtchFileId();
        if (atchFileIdStr != null && !atchFileIdStr.isBlank()) {
            try {
                List<?> files = fileUploadService.retrieveFilesByGroupId(Integer.parseInt(atchFileIdStr));
                model.addAttribute("existingFiles", files != null ? files : List.of());
            } catch (NumberFormatException ignored) {
                model.addAttribute("existingFiles", List.of());
            }
        } else {
            model.addAttribute("existingFiles", List.of());
        }
        return "admin:/instructor/board/form-instructor-board";
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
            RedirectAttributes redirectAttributes) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        instructorBoardDto.setInstrUserId(userId);
        instructorBoardDto.setLastMdfrId(userId);
        instructorBoardDto.setPostCn(TipTapSanitizer.clean(instructorBoardDto.getPostCn()));

        if (error.hasErrors()) {
            String errorMsg = error.getAllErrors().stream().map(e -> e.getDefaultMessage())
                    .findFirst().orElse("입력값을 확인해주세요.");
            redirectAttributes.addFlashAttribute("board", instructorBoardDto);
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:" + updateFormUrl(instructorBoardDto.getPostSn(), listPage, listKeyword, listBoardTypeCd);
        }

        if (isBlankHtml(instructorBoardDto.getPostCn())) {
            redirectAttributes.addFlashAttribute("board", instructorBoardDto);
            redirectAttributes.addFlashAttribute("errorMessage", "내용을 입력해주세요.");
            return "redirect:" + updateFormUrl(instructorBoardDto.getPostSn(), listPage, listKeyword, listBoardTypeCd);
        }

        // 신규 파일 그룹이 생성된 경우에만 실패 시 정리 (기존 그룹 추가는 롤백 불가)
        int newGroupId = -1;
        if (hasFiles(attachFiles)) {
            Long existingAtchFileId = instructorBoardDto.getAtchFileId();
            if (existingAtchFileId != null) {
                int groupId = existingAtchFileId.intValue();
                for (MultipartFile f : attachFiles) {
                    if (!f.isEmpty()) {
                        fileUploadService.uploadFile(f, userId, groupId, FileCtxType.INSTRUCTOR, String.valueOf(groupId));
                    }
                }
            } else {
                newGroupId = fileUploadService.createFileGroup();
                instructorBoardDto.setAtchFileId((long) newGroupId);
                for (MultipartFile f : attachFiles) {
                    if (!f.isEmpty()) {
                        fileUploadService.uploadFile(f, userId, newGroupId, FileCtxType.INSTRUCTOR, String.valueOf(newGroupId));
                    }
                }
            }
        }

        try {
            int rowcnt = instructorBoardService.updateInstructorBoard(instructorBoardDto);
            if (rowcnt > 0) {
                return "redirect:" + detailUrl(instructorBoardDto.getPostSn(), listPage, listKeyword, listBoardTypeCd);
            } else {
                cleanupFileGroup(newGroupId, userId);
                redirectAttributes.addFlashAttribute("board", instructorBoardDto);
                redirectAttributes.addFlashAttribute("errorMessage", "게시글 수정에 실패했습니다. 다시 시도해주세요.");
                return "redirect:" + updateFormUrl(instructorBoardDto.getPostSn(), listPage, listKeyword, listBoardTypeCd);
            }
        } catch (Exception e) {
            log.error("게시글 수정 중 오류 발생", e);
            cleanupFileGroup(newGroupId, userId);
            redirectAttributes.addFlashAttribute("board", instructorBoardDto);
            redirectAttributes.addFlashAttribute("errorMessage", "게시글 수정 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "redirect:" + updateFormUrl(instructorBoardDto.getPostSn(), listPage, listKeyword, listBoardTypeCd);
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
            RedirectAttributes redirectAttributes) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        instructorBoardService.deleteInstructorBoard(postSn, userId);
        redirectAttributes.addFlashAttribute("successMessage", "게시글이 삭제되었습니다.");
        return "redirect:" + detailUrl(postSn, listPage, listKeyword, listBoardTypeCd);
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
            RedirectAttributes redirectAttributes) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        int rows = instructorBoardService.answerInstructorQna(postSn, userId, answCn);
        if (rows == 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Q&A 게시글에만 답변을 등록할 수 있습니다.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "답변이 등록되었습니다.");
        }
        return "redirect:" + detailUrl(postSn, listPage, listKeyword, listBoardTypeCd);
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
            RedirectAttributes redirectAttributes) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        instructorBoardService.restoreInstructorBoard(postSn, userId);
        redirectAttributes.addFlashAttribute("successMessage", "게시글이 복구되었습니다.");
        return "redirect:" + detailUrl(postSn, listPage, listKeyword, listBoardTypeCd);
    }

    private String detailUrl(Long postSn, int page, String keyword, String boardTypeCd) {
        return UriComponentsBuilder.fromPath("/instructor/board/detail/{postSn}")
                .queryParam("page", page)
                .queryParam("keyword", keyword)
                .queryParam("boardTypeCd", boardTypeCd)
                .buildAndExpand(postSn)
                .toUriString();
    }

    private String insertFormUrl(int page, String keyword, String boardTypeCd) {
        return UriComponentsBuilder.fromPath("/instructor/board/insertForm")
                .queryParam("page", page)
                .queryParam("keyword", keyword)
                .queryParam("boardTypeCd", boardTypeCd)
                .toUriString();
    }

    private String updateFormUrl(Long postSn, int page, String keyword, String boardTypeCd) {
        return UriComponentsBuilder.fromPath("/instructor/board/updateForm/{postSn}")
                .queryParam("page", page)
                .queryParam("keyword", keyword)
                .queryParam("boardTypeCd", boardTypeCd)
                .buildAndExpand(postSn)
                .toUriString();
    }

    private boolean hasFiles(List<MultipartFile> files) {
        return files != null && !files.isEmpty() && !files.get(0).isEmpty();
    }

    // groupId > 0 인 경우에만 DB 파일 레코드를 소프트 삭제한다 (외부 파일 서버 파일은 남음)
    private void cleanupFileGroup(int groupId, String userId) {
        if (groupId <= 0) {
            return;
        }
        try {
            fileUploadService.retrieveFilesByGroupId(groupId)
                    .forEach(f -> fileUploadService.removeFile(f.getAtchFileDtlSn(), userId));
        } catch (Exception ex) {
            log.warn("파일 그룹 정리 실패 (groupId={}): {}", groupId, ex.getMessage());
        }
    }

    // TipTap이 빈 상태에서 <p></p> 같은 HTML을 보내므로 텍스트 노드 유무로 판별
    private boolean isBlankHtml(String html) {
        if (html == null || html.isBlank()) {
            return true;
        }
        return Jsoup.parse(html).text().isBlank();
    }


}
