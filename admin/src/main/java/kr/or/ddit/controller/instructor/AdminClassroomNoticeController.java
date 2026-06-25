package kr.or.ddit.controller.instructor;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.or.ddit.finalProject.dto.file.FileCtxType;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardDto;
import kr.or.ddit.finalProject.service.assignment.AssignmentBoardService;
import kr.or.ddit.finalProject.service.classroom.ClassroomService;
import kr.or.ddit.finalProject.service.file.FileUploadService;
import kr.or.ddit.finalProject.service.instructor.InstructorBoardService;
import kr.or.ddit.finalProject.util.TipTapSanitizer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/classroom")
public class AdminClassroomNoticeController extends AbstractClassroomController {

    private static final int PAGE_SIZE = 10;

    private final FileUploadService fileUploadService;

    public AdminClassroomNoticeController(ClassroomService classroomService,
                                          AssignmentBoardService assignmentBoardService,
                                          InstructorBoardService instructorBoardService,
                                          FileUploadService fileUploadService) {
        super(classroomService, assignmentBoardService, instructorBoardService);
        this.fileUploadService = fileUploadService;
    }

    // 공지사항 목록
    @GetMapping("/detail/{classSn}/notice")
    public String noticeList(@PathVariable Long classSn,
            @RequestParam(defaultValue = "1") int page,
            Model model, Authentication authentication) {
        kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) return "redirect:/classroom/list";
        kr.or.ddit.finalProject.dto.common.PageResponse<kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardDto> noticePage =
                instructorBoardService.getClassroomNoticeList(classSn, page, PAGE_SIZE);
        model.addAttribute("classroom", classroom);
        model.addAttribute("noticePage", noticePage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("totalPages", (int) Math.ceil((double) noticePage.getTotalCount() / PAGE_SIZE));
        return "classroom/list-classroom-notices";
    }

    // 공지사항 상세 (첨부파일 포함)
    @GetMapping("/detail/{classSn}/notice/{postSn}")
    public String noticeDetail(@PathVariable Long classSn, @PathVariable Long postSn, Model model,
            Authentication authentication) {
        kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) return "redirect:/classroom/list";
        model.addAttribute("classroom", classroom);
        InstructorBoardDto notice = instructorBoardService.getClassroomNoticeDetail(postSn, classSn);
        model.addAttribute("notice", notice);
        if (notice != null && notice.getAtchFileId() != null) {
            model.addAttribute("attachedFiles",
                    fileUploadService.retrieveFilesByGroupId(notice.getAtchFileId().intValue()));
        } else {
            model.addAttribute("attachedFiles", Collections.emptyList());
        }
        return "classroom/detail-classroom-notice";
    }

    // 공지사항 작성 폼
    @GetMapping("/detail/{classSn}/notice/write")
    public String noticeWriteForm(@PathVariable Long classSn, Model model,
            Authentication authentication) {
        kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) return "redirect:/classroom/list";
        model.addAttribute("classroom", classroom);
        return "classroom/form-classroom-notice";
    }

    // 공지사항 등록 (파일 업로드 실패 시 공지 롤백)
    @PostMapping("/detail/{classSn}/notice/write")
    public String noticeWrite(@PathVariable Long classSn, @ModelAttribute InstructorBoardDto dto,
            @RequestParam(required = false) List<MultipartFile> attachFiles,
            Authentication authentication, RedirectAttributes redirectAttrs) {
        String userId = authentication.getName();
        if (getOwnedClassroom(classSn, userId) == null) return "redirect:/classroom/list";
        dto.setClassSn(classSn);
        dto.setInstrUserId(userId);
        dto.setWrtrUserId(userId);
        dto.setPostCn(TipTapSanitizer.clean(dto.getPostCn()));
        instructorBoardService.insertClassroomNotice(dto);

        if (hasFiles(attachFiles)) {
            int groupId = -1;
            try {
                groupId = fileUploadService.createFileGroup();
                for (MultipartFile f : attachFiles) {
                    if (!f.isEmpty()) {
                        fileUploadService.uploadFile(f, userId, groupId, FileCtxType.INSTRUCTOR, String.valueOf(groupId));
                    }
                }
                dto.setAtchFileId((long) groupId);
                instructorBoardService.updateClassroomNotice(dto);
            } catch (Exception e) {
                log.error("공지 파일 업로드 실패 — 등록된 공지 보상 삭제 (postSn={}, groupId={})", dto.getPostSn(), groupId, e);
                cleanupFileGroup(groupId, userId);
                instructorBoardService.deleteClassroomNotice(dto.getPostSn(), classSn);
                redirectAttrs.addFlashAttribute("toastMsg", "파일 업로드에 실패했습니다.");
                redirectAttrs.addFlashAttribute("toastType", "error");
                return "redirect:/classroom/detail/" + classSn + "/notice/write?error=fileUploadFailed";
            }
        }

        redirectAttrs.addFlashAttribute("toastMsg", "공지사항이 등록되었습니다.");
        return "redirect:/classroom/detail/" + classSn + "/notice/" + dto.getPostSn();
    }

    // 공지사항 수정 폼 (기존 첨부파일 목록 포함)
    @GetMapping("/detail/{classSn}/notice/{postSn}/edit")
    public String noticeEditForm(@PathVariable Long classSn, @PathVariable Long postSn, Model model,
            Authentication authentication) {
        kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) return "redirect:/classroom/list";
        model.addAttribute("classroom", classroom);
        InstructorBoardDto editNotice = instructorBoardService.getClassroomNoticeDetail(postSn, classSn);
        model.addAttribute("editNotice", editNotice);
        if (editNotice != null && editNotice.getAtchFileId() != null) {
            model.addAttribute("existingFiles",
                    fileUploadService.retrieveFilesByGroupId(editNotice.getAtchFileId().intValue()));
        } else {
            model.addAttribute("existingFiles", Collections.emptyList());
        }
        return "classroom/form-classroom-notice";
    }

    // 공지사항 수정 저장 (파일 추가·그룹 신규 생성 모두 처리, 실패 시 신규 파일 롤백)
    @PostMapping("/detail/{classSn}/notice/{postSn}/edit")
    public String noticeEdit(@PathVariable Long classSn, @PathVariable Long postSn,
            @ModelAttribute InstructorBoardDto dto,
            @RequestParam(required = false) List<MultipartFile> attachFiles,
            Authentication authentication, RedirectAttributes redirectAttrs) {
        String userId = authentication.getName();
        if (getOwnedClassroom(classSn, userId) == null) return "redirect:/classroom/list";
        dto.setPostSn(postSn);
        dto.setClassSn(classSn);
        dto.setWrtrUserId(userId);
        dto.setInstrUserId(userId);
        dto.setPostCn(TipTapSanitizer.clean(dto.getPostCn()));

        int newGroupId = -1;
        List<Integer> addedFileSns = Collections.emptyList();
        String editUrl = "redirect:/classroom/detail/" + classSn + "/notice/" + postSn;
        String editErrorUrl = editUrl + "/edit?error=fileUploadFailed";
        if (hasFiles(attachFiles)) {
            Long existingGroupId = dto.getAtchFileId();
            if (existingGroupId != null) {
                List<Integer> before = fileUploadService.retrieveFilesByGroupId(existingGroupId.intValue())
                        .stream().map(f -> f.getAtchFileDtlSn()).toList();
                try {
                    for (MultipartFile f : attachFiles) {
                        if (!f.isEmpty()) {
                            fileUploadService.uploadFile(f, userId, existingGroupId.intValue(),
                                    FileCtxType.INSTRUCTOR, String.valueOf(existingGroupId));
                        }
                    }
                } catch (Exception e) {
                    log.error("공지 수정 파일 업로드 실패 (postSn={}, groupId={})", postSn, existingGroupId, e);
                    fileUploadService.retrieveFilesByGroupId(existingGroupId.intValue())
                            .stream().map(f -> f.getAtchFileDtlSn())
                            .filter(sn -> !before.contains(sn))
                            .forEach(sn -> fileUploadService.removeFile(sn, userId));
                    redirectAttrs.addFlashAttribute("toastMsg", "파일 업로드에 실패했습니다.");
                    redirectAttrs.addFlashAttribute("toastType", "error");
                    return editErrorUrl;
                }
                addedFileSns = fileUploadService.retrieveFilesByGroupId(existingGroupId.intValue())
                        .stream().map(f -> f.getAtchFileDtlSn())
                        .filter(sn -> !before.contains(sn)).toList();
            } else {
                try {
                    newGroupId = fileUploadService.createFileGroup();
                    dto.setAtchFileId((long) newGroupId);
                    for (MultipartFile f : attachFiles) {
                        if (!f.isEmpty()) {
                            fileUploadService.uploadFile(f, userId, newGroupId,
                                    FileCtxType.INSTRUCTOR, String.valueOf(newGroupId));
                        }
                    }
                } catch (Exception e) {
                    log.error("공지 수정 파일 업로드 실패 — 새 그룹 정리 (postSn={}, groupId={})", postSn, newGroupId, e);
                    cleanupFileGroup(newGroupId, userId);
                    redirectAttrs.addFlashAttribute("toastMsg", "파일 업로드에 실패했습니다.");
                    redirectAttrs.addFlashAttribute("toastType", "error");
                    return editErrorUrl;
                }
            }
        }

        try {
            instructorBoardService.updateClassroomNotice(dto);
            redirectAttrs.addFlashAttribute("toastMsg", "공지사항이 수정되었습니다.");
        } catch (Exception e) {
            log.error("공지 수정 DB 반영 실패 (postSn={}, groupId={})", postSn, newGroupId, e);
            addedFileSns.forEach(sn -> fileUploadService.removeFile(sn, userId));
            cleanupFileGroup(newGroupId, userId);
            redirectAttrs.addFlashAttribute("toastMsg", "수정에 실패했습니다.");
            redirectAttrs.addFlashAttribute("toastType", "error");
            return editErrorUrl;
        }

        return editUrl;
    }

    // 공지사항 첨부파일 단건 삭제 (공지 소유권 검증 후 제거)
    @PostMapping("/detail/{classSn}/notice/{postSn}/file/{fileDtlSn}/delete")
    public String noticeFileDelete(@PathVariable Long classSn, @PathVariable Long postSn,
            @PathVariable Integer fileDtlSn, Authentication authentication, RedirectAttributes redirectAttrs) {
        String editUrl = "redirect:/classroom/detail/" + classSn + "/notice/" + postSn + "/edit";
        InstructorBoardDto notice = instructorBoardService.getClassroomNoticeDetail(postSn, classSn);
        if (notice == null || notice.getAtchFileId() == null) {
            return editUrl;
        }
        boolean owned = fileUploadService.retrieveFilesByGroupId(notice.getAtchFileId().intValue())
                .stream().anyMatch(f -> fileDtlSn.equals(f.getAtchFileDtlSn()));
        if (!owned) {
            log.warn("파일 소유권 불일치 — 삭제 거부 (postSn={}, fileDtlSn={})", postSn, fileDtlSn);
            return editUrl;
        }
        fileUploadService.removeFile(fileDtlSn, authentication.getName());
        redirectAttrs.addFlashAttribute("toastMsg", "파일이 삭제되었습니다.");
        return editUrl;
    }

    // 공지사항 삭제
    @PostMapping("/detail/{classSn}/notice/{postSn}/delete")
    public String noticeDelete(@PathVariable Long classSn, @PathVariable Long postSn,
            Authentication authentication, RedirectAttributes redirectAttrs) {
        if (getOwnedClassroom(classSn, authentication.getName()) == null) return "redirect:/classroom/list";
        instructorBoardService.deleteClassroomNotice(postSn, classSn);
        redirectAttrs.addFlashAttribute("toastMsg", "공지사항이 삭제되었습니다.");
        return "redirect:/classroom/detail/" + classSn + "/notice";
    }

    private boolean hasFiles(List<MultipartFile> files) {
        return files != null && files.stream().anyMatch(f -> !f.isEmpty());
    }

    private void cleanupFileGroup(int groupId, String userId) {
        if (groupId <= 0) return;
        try {
            fileUploadService.retrieveFilesByGroupId(groupId)
                    .forEach(f -> fileUploadService.removeFile(f.getAtchFileDtlSn(), userId));
        } catch (Exception ex) {
            log.warn("파일 그룹 정리 실패 (groupId={}): {}", groupId, ex.getMessage());
        }
    }
}
