package kr.or.ddit.controller.instructor;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.or.ddit.finalProject.dto.learning.LearningOverviewDto;
import kr.or.ddit.finalProject.service.learning.LearningOverviewService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/instructor/learning/overview")
@RequiredArgsConstructor
public class InstructorLearningOverviewController {

    private final LearningOverviewService learningOverviewService;

    @GetMapping
    public String overviewPage(Model model, Authentication authentication) {
        String userId = authentication.getName();
        List<LearningOverviewDto> overviewList =
                learningOverviewService.retrieveOverviewByInstructor(userId);
        model.addAttribute("overviewList", overviewList);
        return "admin:/instructor/learningOverview";
    }
}
