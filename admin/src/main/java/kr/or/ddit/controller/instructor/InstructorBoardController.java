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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import kr.or.ddit.finalProject.dto.common.CommonCodeDto;
import kr.or.ddit.finalProject.dto.file.FileCtxType;
import kr.or.ddit.finalProject.dto.instructor.board.BoardType;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardDto;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardResponse;
import kr.or.ddit.finalProject.service.file.FileUploadService;
import kr.or.ddit.finalProject.service.instructor.InstructorBoardService;
import kr.or.ddit.finalProject.util.TipTapSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 강사 게시판 관리 컨트롤러 (관리자 페이지).
 * 강사 홈페이지 게시판의 목록/상세/등록/수정/삭제(소프트)/복구와 Q&A 답변을 처리한다.
 * 클래스룸 전속 게시판(CLASS_SN 값 있음)은 별도 컨트롤러에서 관리한다.
 */
@Slf4j
@Controller
@RequestMapping("/instructor/board")
@RequiredArgsConstructor
public class InstructorBoardController {

    private static final int PAGE_SIZE = 10;

    /** 게시판 분류 목록 — 앱 기동 시 한 번만 생성 (불변) */
    private static final List<CommonCodeDto> BOARD_TYPE_LIST = Arrays.stream(BoardType.values())
            .map(t -> {
                CommonCodeDto dto = new CommonCodeDto();
                dto.setComCd(t.name());
                dto.setComCdNm(t.getLabel());
                return dto;
            })
            .toList();

    private final InstructorBoardService instructorBoardService;
    private final FileUploadService fileUploadService;

    // ── 목록 ─────────────────────────────────────────────────────────

    @GetMapping("/list")
    public String getBoardList(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String boardTypeCd,
            @RequestParam(defaultValue = "") String searchType,
            @RequestParam(defaultValue = "1") int page,
            Model model) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (page < 1) page = 1;
        var result = instructorBoardService.getInstructorBoardList(
                userId, keyword, boardTypeCd, searchType, page, PAGE_SIZE);
        int totalPages = (int) Math.ceil((double) result.getTotalCount() / PAGE_SIZE);
        model.addAttribute("boardList", result.getItems());
        model.addAttribute("totalCount", result.getTotalCount());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", Math.max(totalPages, 1));
        model.addAttribute("keyword", keyword);
        model.addAttribute("boardTypeCd", boardTypeCd);
        model.addAttribute("searchType", searchType);
        model.addAttribute("boardTypes", BOARD_TYPE_LIST);
        return "admin:/instructor/board/list-instructor-board";
    }

    // ── 상세 ─────────────────────────────────────────────────────────

    @GetMapping("/detail/{postSn}")
    public String getBoardDetail(@PathVariable Long postSn,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String boardTypeCd,
            @RequestParam(defaultValue = "") String searchType,
            Model model) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        InstructorBoardResponse board = instructorBoardService.getInstructorBoardDetail(postSn, userId);
        if (board == null) {
            return "redirect:/instructor/board/list";
        }
        model.addAttribute("board", board);
        model.addAttribute("listPage", page);
        model.addAttribute("listKeyword", keyword);
        model.addAttribute("listBoardTypeCd", boardTypeCd);
        model.addAttribute("listSearchType", searchType);
        return "admin:/instructor/board/detail-instructor-board";
    }

    // ── 등록 폼 ──────────────────────────────────────────────────────

    @GetMapping("/insertForm")
    public String getInsertForm(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String boardTypeCd,
            @RequestParam(defaultValue = "") String searchType,
            Model model) {
        model.addAttribute("boardTypes", BOARD_TYPE_LIST);
        model.addAttribute("listPage", page);
        model.addAttribute("listKeyword", keyword);
        model.addAttribute("listBoardTypeCd", boardTypeCd);
        model.addAttribute("listSearchType", searchType);
        return "admin:/instructor/board/form-instructor-board";
    }

    // ── 등록 처리 ─────────────────────────────────────────────────────

    @PostMapping("/insert")
    public String insertBoard(@Validated @ModelAttribute InstructorBoardDto instructorBoardDto,
            BindingResult error,
            @RequestParam(required = false) List<MultipartFile> attachFiles,
            @RequestParam(defaultValue = "1") int listPage,
            @RequestParam(defaultValue = "") String listKeyword,
            @RequestParam(defaultValue = "") String listBoardTypeCd,
            @RequestParam(defaultValue = "") String listSearchType,
            RedirectAttributes ra) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        instructorBoardDto.setInstrUserId(userId);
        instructorBoardDto.setWrtrUserId(userId);
        instructorBoardDto.setPostCn(TipTapSanitizer.clean(instructorBoardDto.getPostCn()));

        if (error.hasErrors()) {
            ra.addFlashAttribute("board", instructorBoardDto);
            ra.addFlashAttribute("errorMessage", firstError(error));
            return "redirect:" + insertFormUrl(listPage, listKeyword, listBoardTypeCd, listSearchType);
        }
        if (isBlankHtml(instructorBoardDto.getPostCn())) {
            ra.addFlashAttribute("board", instructorBoardDto);
            ra.addFlashAttribute("errorMessage", "내용을 입력해주세요.");
            return "redirect:" + insertFormUrl(listPage, listKeyword, listBoardTypeCd, listSearchType);
        }
        // QNA는 학생이 작성하는 분류 — 강사(관리자)가 직접 생성 불가
        if ("QNA".equals(instructorBoardDto.getBoardTypeCd())) {
            ra.addFlashAttribute("board", instructorBoardDto);
            ra.addFlashAttribute("errorMessage", "Q&A 게시글은 직접 작성할 수 없습니다.");
            return "redirect:" + insertFormUrl(listPage, listKeyword, listBoardTypeCd, listSearchType);
        }

        // 1단계: DB INSERT — 파일 업로드 전에 실패하면 고아 파일이 생기지 않는다
        int rowcnt;
        try {
            rowcnt = instructorBoardService.insertInstructorBoard(instructorBoardDto);
        } catch (Exception e) {
            log.error("게시글 등록 중 오류 발생", e);
            ra.addFlashAttribute("board", instructorBoardDto);
            ra.addFlashAttribute("errorMessage", "게시글 등록 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "redirect:" + insertFormUrl(listPage, listKeyword, listBoardTypeCd, listSearchType);
        }
        if (rowcnt <= 0) {
            ra.addFlashAttribute("board", instructorBoardDto);
            ra.addFlashAttribute("errorMessage", "게시글 등록에 실패했습니다. 다시 시도해주세요.");
            return "redirect:" + insertFormUrl(listPage, listKeyword, listBoardTypeCd, listSearchType);
        }

        // 2단계: INSERT 성공 후 파일 업로드 → atchFileId UPDATE
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
                log.error("파일 업로드 실패 — 등록된 게시글 보상 삭제 (postSn={}, groupId={})",
                        instructorBoardDto.getPostSn(), groupId, e);
                cleanupFileGroup(groupId, userId);
                instructorBoardService.deleteInstructorBoard(instructorBoardDto.getPostSn(), userId);
                ra.addFlashAttribute("errorMessage", "파일 업로드에 실패했습니다. 다시 시도해주세요.");
                return "redirect:" + insertFormUrl(listPage, listKeyword, listBoardTypeCd, listSearchType);
            }
        }
        return "redirect:" + detailUrl(instructorBoardDto.getPostSn(), listPage, listKeyword, listBoardTypeCd, listSearchType);
    }

    // ── 수정 폼 ──────────────────────────────────────────────────────

    @GetMapping("/updateForm/{postSn}")
    public String getUpdateForm(@PathVariable Long postSn,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String boardTypeCd,
            @RequestParam(defaultValue = "") String searchType,
            Model model) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        InstructorBoardResponse responseDto = instructorBoardService.getInstructorBoardDetail(postSn, userId);
        if (responseDto == null) {
            return "redirect:/instructor/board/list";
        }
        // QNA는 학생 작성 글 — 강사가 수정할 수 없으므로 상세로 리다이렉트
        if ("QNA".equals(responseDto.getBoardTypeCd())) {
            return "redirect:" + detailUrl(postSn, page, keyword, boardTypeCd, searchType);
        }
        Long atchFileId = null;
        if (responseDto.getAtchFileId() != null && !responseDto.getAtchFileId().isBlank()) {
            try { atchFileId = Long.parseLong(responseDto.getAtchFileId()); } catch (NumberFormatException ignored) {}
        }
        InstructorBoardDto board = InstructorBoardDto.builder()
                .postSn(responseDto.getPostSn())
                .boardTypeCd(responseDto.getBoardTypeCd())
                .postSj(responseDto.getTitle())
                .postCn(responseDto.getContent())
                .atchFileId(atchFileId)
                .build();
        // flash attribute가 있으면(검증 실패 후 리다이렉트) 사용자 입력값을 유지
        if (!model.containsAttribute("board")) {
            model.addAttribute("board", board);
        }
        model.addAttribute("boardTypes", BOARD_TYPE_LIST);
        model.addAttribute("listPage", page);
        model.addAttribute("listKeyword", keyword);
        model.addAttribute("listBoardTypeCd", boardTypeCd);
        model.addAttribute("listSearchType", searchType);
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

    // ── 수정 처리 ─────────────────────────────────────────────────────

    @PostMapping("/update")
    public String updateBoard(@Validated @ModelAttribute InstructorBoardDto instructorBoardDto,
            BindingResult error,
            @RequestParam(required = false) List<MultipartFile> attachFiles,
            @RequestParam(defaultValue = "1") int listPage,
            @RequestParam(defaultValue = "") String listKeyword,
            @RequestParam(defaultValue = "") String listBoardTypeCd,
            @RequestParam(defaultValue = "") String listSearchType,
            RedirectAttributes ra) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        instructorBoardDto.setInstrUserId(userId);
        instructorBoardDto.setLastMdfrId(userId);
        instructorBoardDto.setPostCn(TipTapSanitizer.clean(instructorBoardDto.getPostCn()));

        if (error.hasErrors()) {
            ra.addFlashAttribute("board", instructorBoardDto);
            ra.addFlashAttribute("errorMessage", firstError(error));
            return "redirect:" + updateFormUrl(instructorBoardDto.getPostSn(), listPage, listKeyword, listBoardTypeCd, listSearchType);
        }
        if (isBlankHtml(instructorBoardDto.getPostCn())) {
            ra.addFlashAttribute("board", instructorBoardDto);
            ra.addFlashAttribute("errorMessage", "내용을 입력해주세요.");
            return "redirect:" + updateFormUrl(instructorBoardDto.getPostSn(), listPage, listKeyword, listBoardTypeCd, listSearchType);
        }

        // DB 원본 재조회: 존재 확인 + boardTypeCd 변경 차단 + QNA 수정 차단
        InstructorBoardResponse currentBoard = instructorBoardService.getInstructorBoardDetail(
                instructorBoardDto.getPostSn(), userId);
        if (currentBoard == null) {
            return "redirect:/instructor/board/list";
        }
        if ("QNA".equals(currentBoard.getBoardTypeCd())) {
            ra.addFlashAttribute("errorMessage", "Q&A 게시글은 수정할 수 없습니다.");
            return "redirect:" + detailUrl(instructorBoardDto.getPostSn(), listPage, listKeyword, listBoardTypeCd, listSearchType);
        }
        // 클라이언트에서 넘어온 boardTypeCd를 무시하고 원본으로 강제 고정
        instructorBoardDto.setBoardTypeCd(currentBoard.getBoardTypeCd());

        int newGroupId = -1;
        List<Integer> addedFileSns = List.of();
        if (hasFiles(attachFiles)) {
            Long existingAtchFileId = instructorBoardDto.getAtchFileId();
            if (existingAtchFileId != null) {
                // 기존 그룹에 파일 추가
                int groupId = existingAtchFileId.intValue();
                List<Integer> before = fileUploadService.retrieveFilesByGroupId(groupId)
                        .stream().map(f -> f.getAtchFileDtlSn()).toList();
                try {
                    for (MultipartFile f : attachFiles) {
                        if (!f.isEmpty()) {
                            fileUploadService.uploadFile(f, userId, groupId, FileCtxType.INSTRUCTOR, String.valueOf(groupId));
                        }
                    }
                } catch (Exception e) {
                    log.error("게시글 수정 파일 업로드 실패 (postSn={}, groupId={})",
                            instructorBoardDto.getPostSn(), groupId, e);
                    // 이번 요청에서 추가된 파일만 롤백
                    fileUploadService.retrieveFilesByGroupId(groupId).stream()
                            .map(f -> f.getAtchFileDtlSn())
                            .filter(sn -> !before.contains(sn))
                            .forEach(sn -> fileUploadService.removeFile(sn, userId));
                    ra.addFlashAttribute("board", instructorBoardDto);
                    ra.addFlashAttribute("errorMessage", "파일 업로드에 실패했습니다. 다시 시도해주세요.");
                    return "redirect:" + updateFormUrl(instructorBoardDto.getPostSn(), listPage, listKeyword, listBoardTypeCd, listSearchType);
                }
                addedFileSns = fileUploadService.retrieveFilesByGroupId(groupId).stream()
                        .map(f -> f.getAtchFileDtlSn())
                        .filter(sn -> !before.contains(sn)).toList();
            } else {
                // 첫 파일 첨부 — 새 그룹 생성
                try {
                    newGroupId = fileUploadService.createFileGroup();
                    instructorBoardDto.setAtchFileId((long) newGroupId);
                    for (MultipartFile f : attachFiles) {
                        if (!f.isEmpty()) {
                            fileUploadService.uploadFile(f, userId, newGroupId, FileCtxType.INSTRUCTOR, String.valueOf(newGroupId));
                        }
                    }
                } catch (Exception e) {
                    log.error("게시글 수정 파일 업로드 실패 — 새 그룹 정리 (postSn={}, groupId={})",
                            instructorBoardDto.getPostSn(), newGroupId, e);
                    cleanupFileGroup(newGroupId, userId);
                    ra.addFlashAttribute("board", instructorBoardDto);
                    ra.addFlashAttribute("errorMessage", "파일 업로드에 실패했습니다. 다시 시도해주세요.");
                    return "redirect:" + updateFormUrl(instructorBoardDto.getPostSn(), listPage, listKeyword, listBoardTypeCd, listSearchType);
                }
            }
        }

        try {
            int rowcnt = instructorBoardService.updateInstructorBoard(instructorBoardDto);
            if (rowcnt > 0) {
                return "redirect:" + detailUrl(instructorBoardDto.getPostSn(), listPage, listKeyword, listBoardTypeCd, listSearchType);
            }
            addedFileSns.forEach(sn -> fileUploadService.removeFile(sn, userId));
            cleanupFileGroup(newGroupId, userId);
            ra.addFlashAttribute("board", instructorBoardDto);
            ra.addFlashAttribute("errorMessage", "게시글 수정에 실패했습니다. 다시 시도해주세요.");
            return "redirect:" + updateFormUrl(instructorBoardDto.getPostSn(), listPage, listKeyword, listBoardTypeCd, listSearchType);
        } catch (Exception e) {
            log.error("게시글 수정 중 오류 발생", e);
            addedFileSns.forEach(sn -> fileUploadService.removeFile(sn, userId));
            cleanupFileGroup(newGroupId, userId);
            ra.addFlashAttribute("board", instructorBoardDto);
            ra.addFlashAttribute("errorMessage", "게시글 수정 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "redirect:" + updateFormUrl(instructorBoardDto.getPostSn(), listPage, listKeyword, listBoardTypeCd, listSearchType);
        }
    }

    // ── 첨부파일 삭제 ─────────────────────────────────────────────────

    @PostMapping("/file/{fileDtlSn}/delete")
    public String deleteFile(@PathVariable Integer fileDtlSn,
            @RequestParam Long postSn,
            @RequestParam(defaultValue = "1") int listPage,
            @RequestParam(defaultValue = "") String listKeyword,
            @RequestParam(defaultValue = "") String listBoardTypeCd,
            @RequestParam(defaultValue = "") String listSearchType) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String redirectUrl = updateFormUrl(postSn, listPage, listKeyword, listBoardTypeCd, listSearchType);
        InstructorBoardResponse board = instructorBoardService.getInstructorBoardDetail(postSn, userId);
        if (board == null || board.getAtchFileId() == null || board.getAtchFileId().isBlank()) {
            return "redirect:" + redirectUrl;
        }
        // 해당 파일이 이 게시글 소유인지 확인 후 삭제
        boolean owned = fileUploadService.retrieveFilesByGroupId(Integer.parseInt(board.getAtchFileId()))
                .stream().anyMatch(f -> fileDtlSn.equals(f.getAtchFileDtlSn()));
        if (!owned) {
            log.warn("파일 소유권 불일치 — 삭제 거부 (postSn={}, fileDtlSn={})", postSn, fileDtlSn);
            return "redirect:" + redirectUrl;
        }
        fileUploadService.removeFile(fileDtlSn, userId);
        return "redirect:" + redirectUrl;
    }

    // ── 삭제 / 복구 ──────────────────────────────────────────────────

    @PostMapping("/delete/{postSn}")
    public String deleteBoard(@PathVariable Long postSn,
            @RequestParam(defaultValue = "1") int listPage,
            @RequestParam(defaultValue = "") String listKeyword,
            @RequestParam(defaultValue = "") String listBoardTypeCd,
            @RequestParam(defaultValue = "") String listSearchType,
            RedirectAttributes ra) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        instructorBoardService.deleteInstructorBoard(postSn, userId);
        ra.addFlashAttribute("successMessage", "게시글이 삭제되었습니다.");
        return "redirect:" + detailUrl(postSn, listPage, listKeyword, listBoardTypeCd, listSearchType);
    }

    @PostMapping("/restore/{postSn}")
    public String restoreBoard(@PathVariable Long postSn,
            @RequestParam(defaultValue = "1") int listPage,
            @RequestParam(defaultValue = "") String listKeyword,
            @RequestParam(defaultValue = "") String listBoardTypeCd,
            @RequestParam(defaultValue = "") String listSearchType,
            RedirectAttributes ra) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        instructorBoardService.restoreInstructorBoard(postSn, userId);
        ra.addFlashAttribute("successMessage", "게시글이 복구되었습니다.");
        return "redirect:" + detailUrl(postSn, listPage, listKeyword, listBoardTypeCd, listSearchType);
    }

    // ── Q&A 답변 ─────────────────────────────────────────────────────

    @PostMapping("/answer/{postSn}")
    public String answerBoard(@PathVariable Long postSn,
            @RequestParam String answCn,
            @RequestParam(defaultValue = "1") int listPage,
            @RequestParam(defaultValue = "") String listKeyword,
            @RequestParam(defaultValue = "") String listBoardTypeCd,
            @RequestParam(defaultValue = "") String listSearchType,
            RedirectAttributes ra) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        int rows = instructorBoardService.answerInstructorQna(postSn, userId, answCn);
        if (rows == 0) {
            ra.addFlashAttribute("errorMessage", "답변 등록에 실패했습니다. 게시글 상태를 확인해주세요.");
        } else {
            ra.addFlashAttribute("successMessage", "답변이 등록되었습니다.");
        }
        return "redirect:" + detailUrl(postSn, listPage, listKeyword, listBoardTypeCd, listSearchType);
    }

    // ── URL 헬퍼 ─────────────────────────────────────────────────────

    private String detailUrl(Long postSn, int page, String keyword, String boardTypeCd, String searchType) {
        return UriComponentsBuilder.fromPath("/instructor/board/detail/{postSn}")
                .queryParam("page", page)
                .queryParam("keyword", keyword)
                .queryParam("boardTypeCd", boardTypeCd)
                .queryParam("searchType", searchType)
                .buildAndExpand(postSn)
                .toUriString();
    }

    private String insertFormUrl(int page, String keyword, String boardTypeCd, String searchType) {
        return UriComponentsBuilder.fromPath("/instructor/board/insertForm")
                .queryParam("page", page)
                .queryParam("keyword", keyword)
                .queryParam("boardTypeCd", boardTypeCd)
                .queryParam("searchType", searchType)
                .toUriString();
    }

    private String updateFormUrl(Long postSn, int page, String keyword, String boardTypeCd, String searchType) {
        return UriComponentsBuilder.fromPath("/instructor/board/updateForm/{postSn}")
                .queryParam("page", page)
                .queryParam("keyword", keyword)
                .queryParam("boardTypeCd", boardTypeCd)
                .queryParam("searchType", searchType)
                .buildAndExpand(postSn)
                .toUriString();
    }

    // ── 내부 유틸 ─────────────────────────────────────────────────────

    /** 첨부파일 목록이 실제로 파일을 포함하고 있는지 확인 */
    private boolean hasFiles(List<MultipartFile> files) {
        return files != null && !files.isEmpty() && !files.get(0).isEmpty();
    }

    /**
     * 파일 그룹 내 모든 파일을 소프트 삭제한다.
     * groupId <= 0 이면 아직 그룹이 생성되지 않은 것이므로 아무 것도 하지 않는다.
     */
    private void cleanupFileGroup(int groupId, String userId) {
        if (groupId <= 0) return;
        try {
            fileUploadService.retrieveFilesByGroupId(groupId)
                    .forEach(f -> fileUploadService.removeFile(f.getAtchFileDtlSn(), userId));
        } catch (Exception ex) {
            log.warn("파일 그룹 정리 실패 (groupId={}): {}", groupId, ex.getMessage());
        }
    }

    /**
     * TipTap이 빈 상태에서도 &lt;p&gt;&lt;/p&gt; 같은 HTML을 전송하므로
     * 텍스트 노드 유무로 실질적인 공백 여부를 판별한다.
     */
    private boolean isBlankHtml(String html) {
        if (html == null || html.isBlank()) return true;
        return Jsoup.parse(html).text().isBlank();
    }

    /** BindingResult에서 첫 번째 오류 메시지를 추출한다 */
    private String firstError(BindingResult error) {
        return error.getAllErrors().stream()
                .map(e -> e.getDefaultMessage())
                .findFirst()
                .orElse("입력값을 확인해주세요.");
    }
}
