package kr.or.ddit.controller.instructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.or.ddit.finalProject.dto.curriculum.CurriculumMasterDto;
import kr.or.ddit.finalProject.service.curriculum.CurriculumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/instructor/curriculum")
@RequiredArgsConstructor
public class InstructorCurriculumController {

    private final CurriculumService curriculumService;

    @GetMapping
    public String curriculumMainPage(Model model, Authentication authentication) {
        String loginInstructorId = resolveLoginUserId(authentication);

        List<CurriculumMasterDto> masterList = curriculumService.retrieveMasterList(loginInstructorId);
        model.addAttribute("masterList", masterList);
        return "admin:/instructor/curriculum";
    }

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<String> saveCurriculum(@RequestBody CurriculumSaveRequest request,
            Authentication authentication) {
        String loginInstructorId = resolveLoginUserId(authentication);

        CurriculumMasterDto masterDto = new CurriculumMasterDto();
        masterDto.setTitle(request.getTitle());
        masterDto.setInstructorId(loginInstructorId);
        masterDto.setRgtrId(loginInstructorId);
        masterDto.setLastMdfrId(loginInstructorId);

        boolean created = curriculumService.createCurriculum(masterDto);
        if (!created) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("FAIL");
        }

        return ResponseEntity.ok("SUCCESS");
    }

    @PutMapping("/modify/{curriculumId}")
    @ResponseBody
    public ResponseEntity<String> modifyCurriculum(@PathVariable Long curriculumId,
            @RequestBody CurriculumSaveRequest request,
            Authentication authentication) {
        String loginInstructorId = resolveLoginUserId(authentication);

        CurriculumMasterDto masterDto = new CurriculumMasterDto();
        masterDto.setCurriculumId(curriculumId);
        masterDto.setTitle(request.getTitle());

        curriculumService.modifyCurriculum(masterDto, loginInstructorId);
        return ResponseEntity.ok("SUCCESS");
    }

    @DeleteMapping("/delete/{curriculumId}")
    @ResponseBody
    public ResponseEntity<String> deleteCurriculum(@PathVariable Long curriculumId,
            Authentication authentication) {
        String loginInstructorId = resolveLoginUserId(authentication);

        curriculumService.removeCurriculumLogically(curriculumId, loginInstructorId);
        return ResponseEntity.ok("SUCCESS");
    }

    private String resolveLoginUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        return authentication.getName();
    }

    @lombok.Data
    public static class CurriculumSaveRequest {
        private String title;
    }
}
