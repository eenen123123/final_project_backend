package kr.or.ddit.controller.example;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import kr.or.ddit.finalProject.dto.board.QnaDto;
import kr.or.ddit.finalProject.service.board.qna.QnaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@Controller
@RequiredArgsConstructor
public class ExampleHtmlController {

    private final QnaService qnaService;

    @GetMapping("/vod")
    public String example1() {

        return "admin:/vod_management";
    }

    @GetMapping("/test/editor")
    public String testEditor(Authentication authentication, Model model) {
        QnaDto qnaDto = qnaService.getById(201l, authentication);
        model.addAttribute("qnaDto", qnaDto);
        return "admin:/board/test";
    }
}
