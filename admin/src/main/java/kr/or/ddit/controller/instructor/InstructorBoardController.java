package kr.or.ddit.controller.instructor;

import java.util.List;

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
import kr.or.ddit.finalProject.mapper.common.CommonCodeMapper;
import kr.or.ddit.finalProject.responseDto.instructor.InstructorBoardResponseDto;
import kr.or.ddit.finalProject.service.instructor.InstructorBoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/instructor/board")
@RequiredArgsConstructor
public class InstructorBoardController {

    private final InstructorBoardService instructorBoardService;
    private final CommonCodeMapper commonCodeMapper;

    @GetMapping("/list")
    public String getBoardList(Model model) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("유저아이디 : {}", userId);
        List<InstructorBoardResponseDto> boardList = instructorBoardService.getInstructorBoardList(userId);
        model.addAttribute("boardList", boardList);
        return "admin:/instructor/boardList";
    }

    @GetMapping("/detail/{postSn}")
    public String getBoardDetail(@PathVariable Long postSn, Model model) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        InstructorBoardResponseDto board = instructorBoardService.getInstructorBoardDetail(postSn, userId);
        if (board == null) {
            return "redirect:/instructor/board/list";
        }
        model.addAttribute("board", board);
        return "admin:/instructor/boardDetail";
    }

    @GetMapping("/boardTypes")
    @ResponseBody
    public List<CommonCodeDto> getBoardTypes() {
        return commonCodeMapper.selectByClCode("100").stream()
                .filter(c -> !"01".equals(c.getComCd()))
                .toList();
    }

    @GetMapping("/insertForm")
    public String getInsertForm() {
        return "admin:/instructor/boardInsertForm";
    }

    @PostMapping("/insert")
    public String insertBoard(@Validated @ModelAttribute InstructorBoardDto instructorBoardDto, BindingResult error, RedirectAttributes redirectAttributes) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        instructorBoardDto.setInstrUserId(userId);
        instructorBoardDto.setWrtrUserId(userId);

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

}
