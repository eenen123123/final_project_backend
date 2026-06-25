package kr.or.ddit.controller.instructor;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import kr.or.ddit.finalProject.dto.instructor.journal.InstructorJournalDto;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.service.instructor.InstructorJournalService;
import lombok.RequiredArgsConstructor;

/**
 * 업무 일지 컨트롤러
 *
 * [URL 구조]
 *   GET  /instructor/journals                    → 목록 (선택 없음)
 *   GET  /instructor/journals?jrnlSn=N           → 목록 + 선택된 일지 상세 (읽기)
 *   GET  /instructor/journals?jrnlSn=N&edit=true → 목록 + 선택된 일지 수정 폼
 *   GET  /instructor/journals?newForm=true        → 목록 + 새 일지 작성 폼
 *   POST /instructor/journals/create             → 일지 등록 → redirect
 *   POST /instructor/journals/{sn}/update        → 일지 수정 → redirect
 *   POST /instructor/journals/{sn}/delete        → 일지 삭제 → redirect
 *
 * [접근 제어]
 *   - 원장(Z001) / 수석 강사(T001): isViewer=true  → 전체 일지 열람, CRUD 불가
 *   - 그 외 강사                  : isViewer=false → 본인 일지 열람 + CRUD
 */
@Controller
@RequestMapping("/instructor/journals")
@RequiredArgsConstructor
public class InstructorJournalController {

    private final InstructorJournalService journalService;

    // ──────────────────────────────────────────────
    // 역할 판별 헬퍼
    // ──────────────────────────────────────────────

    /** 원장(Z001) 또는 팀 매니저(T001/T002/T003)이면 팀 일지 열람 뷰어로 처리 */
    private boolean isViewer(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> {
                    String role = a.getAuthority();
                    return "Z001".equals(role)
                        || "T001".equals(role)
                        || "T002".equals(role)
                        || "T003".equals(role);
                });
    }

    /** 원장(Z001)만 쓰기 불가 — 팀 매니저는 본인 일지 CRUD 허용 */
    private boolean isReadOnly(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> "Z001".equals(a.getAuthority()));
    }

    /**
     * 팀 필터 적용 여부 결정.
     * - 원장(Z001): null 반환 → 전체 강사 조회
     * - 팀 매니저(T001/T002/T003): 본인 USER_ID 반환 → CONNECT BY로 팀원만 조회
     * - 일반 강사: null 반환 (isViewer=false이므로 서비스에서 본인 ID로 고정)
     */
    private String resolveMgrUserId(Authentication auth) {
        boolean isPrincipal = auth.getAuthorities().stream()
                .anyMatch(a -> "Z001".equals(a.getAuthority()));
        if (isPrincipal) return null;

        boolean isTeamMgr = auth.getAuthorities().stream()
                .anyMatch(a -> {
                    String role = a.getAuthority();
                    return "T001".equals(role) || "T002".equals(role) || "T003".equals(role);
                });
        return isTeamMgr ? auth.getName() : null;
    }

    // ──────────────────────────────────────────────
    // 목록 + 상세 페이지 (2-pane)
    // ──────────────────────────────────────────────

    @GetMapping
    public String journalPage(
            @RequestParam(required = false) Long jrnlSn,
            @RequestParam(required = false, defaultValue = "false") boolean edit,
            @RequestParam(required = false, defaultValue = "false") boolean newForm,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String fromDt,
            @RequestParam(required = false) String toDt,
            @RequestParam(required = false) String selectedInstrId,
            @RequestParam(required = false, defaultValue = "1") int page,
            Model model,
            Authentication auth) {

        String userId     = auth.getName();
        boolean isViewer  = isViewer(auth);
        boolean isReadOnly = isReadOnly(auth);
        String mgrUserId  = resolveMgrUserId(auth);

        // 좌측 패널: 역할 + 필터 + 페이지에 따라 목록 조회
        List<InstructorJournalDto> journalList =
                journalService.retrieveJournalList(userId, isViewer, mgrUserId, selectedInstrId, keyword, fromDt, toDt, page);
        int totalCount  = journalService.retrieveJournalCount(userId, isViewer, mgrUserId, selectedInstrId, keyword, fromDt, toDt);
        int totalPages  = (int) Math.ceil((double) totalCount / InstructorJournalService.PAGE_SIZE);
        if (totalPages < 1) totalPages = 1;

        model.addAttribute("journalList", journalList);
        model.addAttribute("isViewer",   isViewer);
        model.addAttribute("isReadOnly", isReadOnly);

        // 필터값 보존 (폼 재입력 + 목록 링크 href 유지용)
        model.addAttribute("keyword",         keyword         != null ? keyword         : "");
        model.addAttribute("fromDt",          fromDt          != null ? fromDt          : "");
        model.addAttribute("toDt",            toDt            != null ? toDt            : "");
        model.addAttribute("selectedInstrId", selectedInstrId != null ? selectedInstrId : "");

        // 뷰어 전용: 강사 선택 드롭다운용 목록 (팀 필터 적용)
        if (isViewer) {
            model.addAttribute("journalInstructors", journalService.retrieveJournalInstructors(mgrUserId));
        }

        // 페이지네이션 정보
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages",  totalPages);
        model.addAttribute("totalCount",  totalCount);

        // 우측 패널 상태 결정
        if (newForm && !isReadOnly) {
            // 새 일지 작성 폼
            model.addAttribute("mode", "new");

        } else if (jrnlSn != null) {
            InstructorJournalDto selected = journalService.retrieveJournalBySn(jrnlSn);

            // 존재하지 않는 jrnlSn이 URL로 들어온 경우 초기 상태로 복귀
            if (selected == null) {
                model.addAttribute("mode", "none");
                return "admin:/instructor/instructorJournal";
            }

            model.addAttribute("selected", selected);

            // 본인 일지 여부 — 원장(readOnly) 제외하고 작성자 본인이면 수정·삭제 허용
            boolean isOwner = !isReadOnly && userId.equals(selected.getInstrUserId());
            model.addAttribute("isOwner", isOwner);

            if (edit && isOwner) {
                model.addAttribute("mode", "edit");
            } else {
                model.addAttribute("mode", "view");
            }

        } else {
            // 초기 상태
            model.addAttribute("mode", "none");
        }

        return "admin:/instructor/instructorJournal";
    }

    // ──────────────────────────────────────────────
    // 일지 등록
    // ──────────────────────────────────────────────

    @PostMapping("/create")
    public String createJournal(@Valid InstructorJournalDto dto,
                                BindingResult bindingResult,
                                Authentication auth,
                                RedirectAttributes ra) {
        if (isReadOnly(auth)) {
            ra.addFlashAttribute("errorMessage", "읽기 전용 권한으로는 일지를 작성할 수 없습니다.");
            return "redirect:/instructor/journals";
        }
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("errorMessage",
                    bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return "redirect:/instructor/journals?newForm=true";
        }
        dto.setInstrUserId(auth.getName());
        Long newSn = journalService.createJournal(dto);
        if (newSn == null) {
            ra.addFlashAttribute("errorMessage", "일지 등록 중 오류가 발생했습니다.");
            return "redirect:/instructor/journals?newForm=true";
        }
        return "redirect:/instructor/journals?jrnlSn=" + newSn;
    }

    // ──────────────────────────────────────────────
    // 일지 수정
    // ──────────────────────────────────────────────

    @PostMapping("/{jrnlSn}/update")
    public String updateJournal(@PathVariable Long jrnlSn,
                                @Valid InstructorJournalDto dto,
                                BindingResult bindingResult,
                                Authentication auth,
                                RedirectAttributes ra) {
        if (isReadOnly(auth)) {
            ra.addFlashAttribute("errorMessage", "읽기 전용 권한으로는 일지를 수정할 수 없습니다.");
            return "redirect:/instructor/journals?jrnlSn=" + jrnlSn;
        }
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("errorMessage",
                    bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return "redirect:/instructor/journals?jrnlSn=" + jrnlSn + "&edit=true";
        }
        dto.setJrnlSn(jrnlSn);
        try {
            journalService.modifyJournal(dto, auth.getName());
        } catch (FinalProjectException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/instructor/journals?jrnlSn=" + jrnlSn + "&edit=true";
        }
        return "redirect:/instructor/journals?jrnlSn=" + jrnlSn;
    }

    // ──────────────────────────────────────────────
    // 일지 삭제
    // ──────────────────────────────────────────────

    @PostMapping("/{jrnlSn}/delete")
    public String deleteJournal(@PathVariable Long jrnlSn,
                                Authentication auth,
                                RedirectAttributes ra) {
        if (isReadOnly(auth)) {
            ra.addFlashAttribute("errorMessage", "읽기 전용 권한으로는 일지를 삭제할 수 없습니다.");
            return "redirect:/instructor/journals?jrnlSn=" + jrnlSn;
        }
        try {
            journalService.removeJournal(jrnlSn, auth.getName());
        } catch (FinalProjectException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/instructor/journals?jrnlSn=" + jrnlSn;
        }
        return "redirect:/instructor/journals";
    }
}
