package kr.or.ddit.controller.instructor;

import java.util.List;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.or.ddit.finalProject.dto.common.CommonCodeDto;
import kr.or.ddit.finalProject.dto.instructor.InstructorBoardDto;
import kr.or.ddit.finalProject.dto.instructor.InstructorBoardResponse;
import kr.or.ddit.finalProject.mapper.common.CommonCodeMapper;
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
    private final CommonCodeMapper commonCodeMapper;

    /**
     * 강사 게시판 목록 조회
     *
     * @param model
     * @return
     */
    @GetMapping("/list")
    public String getBoardList(Model model) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("유저아이디 : {}", userId);
        List<InstructorBoardResponse> boardList = instructorBoardService.getInstructorBoardList(userId);
        model.addAttribute("boardList", boardList);
        return "admin:/instructor/boardList";
    }

    /**
     * 게시판 유형 조회 (AJAX)
     *
     * @return
     */
    @GetMapping("/boardTypes")
    @ResponseBody
    public List<CommonCodeDto> getBoardTypes() {
        return commonCodeMapper.selectByClCode("100").stream()
                .filter(c -> !"01".equals(c.getComCd()))
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
    public String getBoardDetail(@PathVariable Long postSn, Model model) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        InstructorBoardResponse board = instructorBoardService.getInstructorBoardDetail(postSn, userId);
        if (board == null) {
            return "redirect:/instructor/board/list";
        }
        board.setContent(sanitize(board.getContent()));
        model.addAttribute("board", board);
        return "admin:/instructor/boardDetail";
    }

    /**
     * 강사 게시판 등록 폼 조회
     *
     * @return
     */
    @GetMapping("/insertForm")
    public String getInsertForm(Model model) {
        model.addAttribute("boardTypes", getBoardTypeList());
        return "admin:/instructor/boardInsertForm";
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
    public String insertBoard(@Validated @ModelAttribute InstructorBoardDto instructorBoardDto, BindingResult error, RedirectAttributes redirectAttributes) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        instructorBoardDto.setInstrUserId(userId);
        instructorBoardDto.setWrtrUserId(userId);
        instructorBoardDto.setPostCn(sanitize(instructorBoardDto.getPostCn()));

        if (error.hasErrors()) {
            String errorMsg = error.getAllErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .findFirst()
                    .orElse("입력값을 확인해주세요.");
            redirectAttributes.addFlashAttribute("board", instructorBoardDto);
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/instructor/board/insertForm";
        }
        try {
            int rowcnt = instructorBoardService.insertInstructorBoard(instructorBoardDto);
            if (rowcnt > 0) {
                return "redirect:/instructor/board/detail/" + instructorBoardDto.getPostSn();
            } else {
                redirectAttributes.addFlashAttribute("board", instructorBoardDto);
                redirectAttributes.addFlashAttribute("errorMessage", "게시글 등록에 실패했습니다. 다시 시도해주세요.");
                return "redirect:/instructor/board/insertForm";
            }
        } catch (Exception e) {
            log.error("게시글 등록 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("board", instructorBoardDto);
            redirectAttributes.addFlashAttribute("errorMessage", "게시글 등록 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "redirect:/instructor/board/insertForm";
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
    public String getUpdateForm(@PathVariable Long postSn, Model model) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        InstructorBoardResponse responseDto = instructorBoardService.getInstructorBoardDetail(postSn, userId);
        if (responseDto == null) {
            return "redirect:/instructor/board/list";
        }
        InstructorBoardDto board = InstructorBoardDto.builder()
                .postSn(responseDto.getPostSn())
                .boardTypeCd(responseDto.getBoardTypeCd())
                .postSj(responseDto.getTitle())
                .postCn(sanitize(responseDto.getContent()))
                .build();
        model.addAttribute("board", board);
        model.addAttribute("boardTypes", getBoardTypeList());
        return "admin:/instructor/boardInsertForm";
    }

    private List<CommonCodeDto> getBoardTypeList() {
        return commonCodeMapper.selectByClCode("100").stream()
                .filter(c -> !"01".equals(c.getComCd()))
                .toList();
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
    public String updateBoard(@Validated @ModelAttribute InstructorBoardDto instructorBoardDto, BindingResult error, RedirectAttributes redirectAttributes) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        instructorBoardDto.setInstrUserId(userId);
        instructorBoardDto.setLastMdfrId(userId);
        instructorBoardDto.setPostCn(sanitize(instructorBoardDto.getPostCn()));

        if (error.hasErrors()) {
            String errorMsg = error.getAllErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .findFirst()
                    .orElse("입력값을 확인해주세요.");
            redirectAttributes.addFlashAttribute("board", instructorBoardDto);
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/instructor/board/updateForm/" + instructorBoardDto.getPostSn();
        }
        try {
            int rowcnt = instructorBoardService.updateInstructorBoard(instructorBoardDto);
            if (rowcnt > 0) {
                return "redirect:/instructor/board/detail/" + instructorBoardDto.getPostSn();
            } else {
                redirectAttributes.addFlashAttribute("board", instructorBoardDto);
                redirectAttributes.addFlashAttribute("errorMessage", "게시글 수정에 실패했습니다. 다시 시도해주세요.");
                return "redirect:/instructor/board/updateForm/" + instructorBoardDto.getPostSn();
            }
        } catch (Exception e) {
            log.error("게시글 수정 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("board", instructorBoardDto);
            redirectAttributes.addFlashAttribute("errorMessage", "게시글 수정 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "redirect:/instructor/board/updateForm/" + instructorBoardDto.getPostSn();
        }
    }

    /**
     * 강사 게시판 삭제 (소프트)
     */
    @PostMapping("/delete/{postSn}")
    public String deleteBoard(@PathVariable Long postSn, RedirectAttributes redirectAttributes) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        instructorBoardService.deleteInstructorBoard(postSn, userId);
        redirectAttributes.addFlashAttribute("successMessage", "게시글이 삭제되었습니다.");
        return "redirect:/instructor/board/detail/" + postSn;
    }

    /**
     * 강사 게시판 복구
     *
     * @param postSn
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/restore/{postSn}")
    public String restoreBoard(@PathVariable Long postSn, RedirectAttributes redirectAttributes) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        instructorBoardService.restoreInstructorBoard(postSn, userId);
        redirectAttributes.addFlashAttribute("successMessage", "게시글이 복구되었습니다.");
        return "redirect:/instructor/board/detail/" + postSn;
    }

    // Toast UI Editor 허용 태그 목록 — 매 호출마다 재생성하지 않도록 static 상수로 선언
    private static final Safelist TOAST_SAFELIST = Safelist.relaxed()
            .addTags("del", "s", "hr", "input") // relaxed 기본 목록에 없는 태그
            .addAttributes("input", "type", "checked", "disabled") // 체크리스트
            .addAttributes("span", "style") // color-syntax 인라인 색상
            .addAttributes("p", "style")
            .addAttributes("h1", "style").addAttributes("h2", "style")
            .addAttributes("h3", "style").addAttributes("h4", "style")
            .addAttributes("h5", "style").addAttributes("h6", "style");

    // XSS 방어: 허용 태그만 남기고 나머지 제거
    private String sanitize(String html) {
        if (html == null) {
            return null;
        }
        return Jsoup.clean(html, TOAST_SAFELIST);
    }

}
