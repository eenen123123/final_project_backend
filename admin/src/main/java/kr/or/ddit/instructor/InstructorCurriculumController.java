package kr.or.ddit.instructor;

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

import kr.or.ddit.dto.CurriculumDetailDto;
import kr.or.ddit.dto.CurriculumMasterDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/instructor/curriculum")
@RequiredArgsConstructor
public class InstructorCurriculumController {

    private final InstructorCurriculumService curriculumService;

    /**
     * 1. 커리큘럼 관리 메인 화면 이동 (공통 레이아웃 규칙 적용)
     */
    @GetMapping
    public String curriculumMainPage(Model model, Authentication authentication) {
        String loginInstructorId = resolveLoginUserId(authentication);

        List<CurriculumMasterDto> masterList = curriculumService.retrieveMasterList(loginInstructorId);
        model.addAttribute("masterList", masterList);

        // [레이아웃 가이드 2-1 규칙] contentPage에 실제 템플릿 경로 설정
        model.addAttribute("contentPage", "instructor/curriculum_main");
        return "admin:/instructor/curriculum";
    }

    /**
     * 2. 특정 커리큘럼의 AG Grid 데이터(상세) 조회 API (AJAX 호출용)
     */
    @GetMapping("/detail/{curriculumId}")
    @ResponseBody
    public ResponseEntity<List<CurriculumDetailDto>> getCurriculumDetail(@PathVariable Long curriculumId,
            Authentication authentication) {
        String loginInstructorId = resolveLoginUserId(authentication);

        List<CurriculumDetailDto> detailList = curriculumService.retrieveDetailList(curriculumId, loginInstructorId);
        return ResponseEntity.ok(detailList);
    }

    /**
     * 3. 커리큘럼 신규 저장 API
     */
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

        applyAuditInfo(request.getDetailList(), loginInstructorId);

        boolean created = curriculumService.createCurriculum(masterDto, request.getDetailList());
        if (!created) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("FAIL");
        }

        return ResponseEntity.ok("SUCCESS");
    }

    /**
     * 4. 커리큘럼 수정 API
     */
    @PutMapping("/modify/{curriculumId}")
    @ResponseBody
    public ResponseEntity<String> modifyCurriculum(@PathVariable Long curriculumId,
            @RequestBody CurriculumSaveRequest request,
            Authentication authentication) {
        String loginInstructorId = resolveLoginUserId(authentication);

        CurriculumMasterDto masterDto = new CurriculumMasterDto();
        masterDto.setCurriculumId(curriculumId);
        masterDto.setTitle(request.getTitle());
        masterDto.setLastMdfrId(loginInstructorId);

        applyAuditInfo(request.getDetailList(), loginInstructorId);

        curriculumService.modifyCurriculum(masterDto, request.getDetailList(), loginInstructorId);
        return ResponseEntity.ok("SUCCESS");
    }

    /**
     * 5. 커리큘럼 소프트 삭제 API
     */
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

    private void applyAuditInfo(List<CurriculumDetailDto> detailList, String currentUserId) {
        if (detailList == null) {
            return;
        }

        for (CurriculumDetailDto detail : detailList) {
            detail.setRgtrId(currentUserId);
            detail.setLastMdfrId(currentUserId);
        }
    }

    /**
     * JSON 바인딩용 내부 DTO 데이터 클래스
     */
    @lombok.Data
    public static class CurriculumSaveRequest {

        private String title;
        private List<CurriculumDetailDto> detailList;
    }
}
