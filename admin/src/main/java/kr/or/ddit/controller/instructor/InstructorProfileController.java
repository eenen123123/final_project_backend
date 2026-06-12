package kr.or.ddit.controller.instructor;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import kr.or.ddit.finalProject.dto.instructor.InstructorCareerDto;
import kr.or.ddit.finalProject.dto.instructor.InstructorCareerSaveRequest;
import kr.or.ddit.finalProject.dto.instructor.InstructorDto;
import kr.or.ddit.finalProject.dto.instructor.InstructorIntroUpdateRequest;
import kr.or.ddit.finalProject.service.instructor.InstructorService;
import lombok.RequiredArgsConstructor;

/**
 * 강사 개인 페이지 관리 컨트롤러
 *
 * [URL 구조] GET /instructor/profile/instructor → 프로필 페이지 조회 POST
 * /instructor/profile/instructor/image → 프로필 이미지 업로드 POST
 * /instructor/profile/instructor/image/delete → 프로필 이미지 제거 POST
 * /instructor/profile/instructor/intro → 소개글 수정 POST
 * /instructor/profile/instructor/careers → 약력 항목 등록 POST
 * /instructor/profile/instructor/careers/{sn}/update → 약력 항목 수정 POST
 * /instructor/profile/instructor/careers/{sn}/delete → 약력 항목 소프트 딜리트
 *
 * [접근 제어] 로그인한 강사 본인의 프로필만 관리합니다. instrUserId는 항상 Authentication에서 추출하며, 요청
 * 파라미터로 받지 않습니다.
 */
@Controller
@RequestMapping("/instructor/profile/instructor")
@RequiredArgsConstructor
public class InstructorProfileController {

    private final InstructorService profileService;

    // ──────────────────────────────────────────────
    // 프로필 페이지 조회
    // ──────────────────────────────────────────────
    @GetMapping
    public String profilePage(Model model, Authentication auth) {
        String instrUserId = auth.getName();

        // 기본 프로필 (소개글 + 이미지 URL)
        // INSTRUCTOR 테이블에 row가 없으면 null이 반환되므로 빈 객체로 방어
        InstructorDto profile = profileService.retrieveProfile(instrUserId);
        if (profile == null) {
            profile = new InstructorDto();
            profile.setInstrUserId(instrUserId);
        }

        // 약력 항목 목록 (DEL_YN='N'인 항목만, 유형별 정렬)
        List<InstructorCareerDto> careers = profileService.retrieveCareers(instrUserId);

        model.addAttribute("profile", profile);
        model.addAttribute("careers", careers);

        return "admin:/instructor/instructorProfile";
    }

    // ──────────────────────────────────────────────
    // 프로필 이미지 업로드
    // ──────────────────────────────────────────────
    @PostMapping("/image")
    public String uploadProfileImage(
            @RequestParam("imageFile") MultipartFile imageFile,
            Authentication auth) {

        profileService.updateProfileImage(auth.getName(), imageFile);
        return "redirect:/instructor/profile/instructor";
    }

    // ──────────────────────────────────────────────
    // 프로필 이미지 제거
    // ──────────────────────────────────────────────
    @PostMapping("/image/delete")
    public String deleteProfileImage(Authentication auth) {
        profileService.removeProfileImage(auth.getName());
        return "redirect:/instructor/profile/instructor";
    }

    // ──────────────────────────────────────────────
    // 소개글 수정
    // ──────────────────────────────────────────────
    @PostMapping("/intro")
    public String updateIntro(InstructorIntroUpdateRequest request, Authentication auth) {
        // 빈 문자열 → null 변환은 서비스에서 처리
        profileService.updateIntro(auth.getName(), request.getInstrIntro());
        return "redirect:/instructor/profile/instructor";
    }

    // ──────────────────────────────────────────────
    // 약력 항목 등록
    // ──────────────────────────────────────────────
    @PostMapping("/careers")
    public String addCareer(InstructorCareerSaveRequest request, Authentication auth) {
        profileService.addCareer(auth.getName(), request);
        return "redirect:/instructor/profile/instructor";
    }

    // ──────────────────────────────────────────────
    // 약력 항목 수정
    // ──────────────────────────────────────────────
    @PostMapping("/careers/{careerSn}/update")
    public String updateCareer(@PathVariable Long careerSn,
            InstructorCareerSaveRequest request,
            Authentication auth) {
        // 소유권 확인은 서비스에서 처리
        profileService.modifyCareer(careerSn, auth.getName(), request);
        return "redirect:/instructor/profile/instructor";
    }

    // ──────────────────────────────────────────────
    // 약력 항목 소프트 딜리트
    // ──────────────────────────────────────────────
    @PostMapping("/careers/{careerSn}/delete")
    public String deleteCareer(@PathVariable Long careerSn, Authentication auth) {
        // 소유권 확인 및 DEL_YN='Y' 처리는 서비스에서 처리
        profileService.removeCareer(careerSn, auth.getName());
        return "redirect:/instructor/profile/instructor";
    }
}
