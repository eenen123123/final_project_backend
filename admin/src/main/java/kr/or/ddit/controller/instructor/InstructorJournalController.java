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

import kr.or.ddit.finalProject.dto.journal.InstructorJournalDto;
import kr.or.ddit.finalProject.service.journal.InstructorJournalService;
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

    /** 원장(Z001) 또는 수석 강사(T001)이면 읽기 전용 뷰어로 처리 */
    private boolean isViewer(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> "Z001".equals(a.getAuthority())
                            || "T001".equals(a.getAuthority()));
    }

    // ──────────────────────────────────────────────
    // 목록 + 상세 페이지 (2-pane)
    // ──────────────────────────────────────────────

    @GetMapping
    public String journalPage(
            @RequestParam(required = false) Long jrnlSn,
            @RequestParam(required = false, defaultValue = "false") boolean edit,
            @RequestParam(required = false, defaultValue = "false") boolean newForm,
            Model model,
            Authentication auth) {

        String userId    = auth.getName();
        boolean isViewer = isViewer(auth);

        // 좌측 패널: 역할에 따라 목록 범위 결정
        List<InstructorJournalDto> journalList =
                journalService.retrieveJournalList(userId, isViewer);
        model.addAttribute("journalList", journalList);
        model.addAttribute("isViewer", isViewer);

        // 우측 패널 상태 결정
        if (newForm && !isViewer) {
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

            // 본인 일지 여부 — 수정·삭제 버튼 표시와 수정 폼 진입에 모두 사용
            boolean isOwner = !isViewer && userId.equals(selected.getInstrUserId());
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
    public String createJournal(InstructorJournalDto dto, Authentication auth) {
        dto.setInstrUserId(auth.getName()); // 작성자는 로그인 사용자로 고정
        Long newSn = journalService.createJournal(dto);
        return "redirect:/instructor/journals?jrnlSn=" + newSn;
    }

    // ──────────────────────────────────────────────
    // 일지 수정
    // ──────────────────────────────────────────────

    @PostMapping("/{jrnlSn}/update")
    public String updateJournal(@PathVariable Long jrnlSn,
                                InstructorJournalDto dto,
                                Authentication auth) {
        dto.setJrnlSn(jrnlSn);
        journalService.modifyJournal(dto, auth.getName()); // 소유권 검증은 서비스에서
        return "redirect:/instructor/journals?jrnlSn=" + jrnlSn;
    }

    // ──────────────────────────────────────────────
    // 일지 삭제
    // ──────────────────────────────────────────────

    @PostMapping("/{jrnlSn}/delete")
    public String deleteJournal(@PathVariable Long jrnlSn, Authentication auth) {
        journalService.removeJournal(jrnlSn, auth.getName()); // 소유권 검증은 서비스에서
        return "redirect:/instructor/journals";
    }
}
