package kr.or.ddit.controller.instructor;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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

    @GetMapping("/list")
    public String getBoardList(Model model) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("유저아이디 : {}", userId);
        List<InstructorBoardResponseDto> boardList = instructorBoardService.getInstructorBoardList(userId);
        model.addAttribute("boardList", boardList);
        return "admin:/instructor/boardList";
    }

    @GetMapping("/detail/{postSn}")
    public String getMethodName(@PathVariable int postSn, Model model) {
        InstructorBoardResponseDto board = instructorBoardService.getInstructorBoardDetail(postSn);
        model.addAttribute("board", board);
        return "admin:/instructor/boardDetail";
    }

}
