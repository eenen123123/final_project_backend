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

import kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse;
import kr.or.ddit.finalProject.dto.file.FileCtxType;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardDto;
import kr.or.ddit.finalProject.service.assignment.AssignmentBoardService;
import kr.or.ddit.finalProject.service.classroom.ClassroomService;
import kr.or.ddit.finalProject.service.file.FileUploadService;
import kr.or.ddit.finalProject.service.instructor.InstructorBoardService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/classroom")
public class AdminClassroomDataroomController extends AbstractClassroomController {

    private static final int PAGE_SIZE = 10;

    private final FileUploadService fileUploadService;

    public AdminClassroomDataroomController(ClassroomService classroomService,
                                            AssignmentBoardService assignmentBoardService,
                                            InstructorBoardService instructorBoardService,
                                            FileUploadService fileUploadService) {
        super(classroomService, assignmentBoardService, instructorBoardService);
        this.fileUploadService = fileUploadService;
    }

    // 자료실 목록
    @GetMapping("/detail/{classSn}/dataroom")
    public String dataroomList(@PathVariable Long classSn,
            @RequestParam(defaultValue = "1") int page,
            Model model, Authentication authentication) {
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) return "redirect:/classroom/list";
        kr.or.ddit.finalProject.dto.common.PageResponse<InstructorBoardDto> dataroomPage =
                instructorBoardService.getClassroomDataroomList(classSn, page, PAGE_SIZE);
        model.addAttribute("classroom", classroom);
        model.addAttribute("dataroomPage", dataroomPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("totalPages", (int) Math.ceil((double) dataroomPage.getTotalCount() / PAGE_SIZE));
        return "classroom/list-classroom-dataroom";
    }

    // 자료실 상세
    @GetMapping("/detail/{classSn}/dataroom/{postSn}")
    public String dataroomDetail(@PathVariable Long classSn, @PathVariable Long postSn,
            Model model, Authentication authentication) {
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) return "redirect:/classroom/list";
        InstructorBoardDto dataroom = instructorBoardService.getClassroomDataroomDetail(postSn, classSn);
        if (dataroom == null) return "redirect:/classroom/detail/" + classSn + "/dataroom";
        model.addAttribute("classroom", classroom);
        model.addAttribute("dataroom", dataroom);
        model.addAttribute("attachedFiles",
                dataroom.getAtchFileId() != null
                        ? fileUploadService.retrieveFilesByGroupId(dataroom.getAtchFileId().intValue())
                        : Collections.emptyList());
        return "classroom/detail-classroom-dataroom";
    }

    // 자료 등록 폼
    @GetMapping("/detail/{classSn}/dataroom/write")
    public String dataroomWriteForm(@PathVariable Long classSn, Model model,
            Authentication authentication) {
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) return "redirect:/classroom/list";
        model.addAttribute("classroom", classroom);
        return "classroom/form-classroom-dataroom";
    }

    // 자료 등록 처리 (파일 없어도 등록 가능, 업로드 실패 시 게시물 롤백)
    @PostMapping("/detail/{classSn}/dataroom/write")
    public String dataroomWrite(@PathVariable Long classSn,
            @ModelAttribute InstructorBoardDto dto,
            @RequestParam(required = false) List<MultipartFile> attachFiles,
            Authentication authentication, RedirectAttributes redirectAttrs) {
        String userId = authentication.getName();
        if (getOwnedClassroom(classSn, userId) == null) return "redirect:/classroom/list";

        dto.setClassSn(classSn);
        dto.setInstrUserId(userId);
        dto.setWrtrUserId(userId);
        instructorBoardService.insertClassroomDataroom(dto);

        if (hasFiles(attachFiles)) {
            int groupId = -1;
            try {
                groupId = fileUploadService.createFileGroup();
                for (MultipartFile f : attachFiles) {
                    if (!f.isEmpty()) {
                        fileUploadService.uploadFile(f, userId, groupId,
                                FileCtxType.INSTRUCTOR, String.valueOf(groupId));
                    }
                }
                dto.setAtchFileId((long) groupId);
                instructorBoardService.updateClassroomDataroom(dto);
            } catch (Exception e) {
                log.error("자료 파일 업로드 실패 — 등록된 자료 보상 삭제 (postSn={}, groupId={})",
                        dto.getPostSn(), groupId, e);
                cleanupFileGroup(groupId, userId);
                instructorBoardService.deleteClassroomDataroom(dto.getPostSn(), classSn);
                redirectAttrs.addFlashAttribute("toastMsg", "파일 업로드에 실패했습니다.");
                redirectAttrs.addFlashAttribute("toastType", "error");
                return "redirect:/classroom/detail/" + classSn + "/dataroom/write?error=fileUploadFailed";
            }
        }

        redirectAttrs.addFlashAttribute("toastMsg", "자료가 등록되었습니다.");
        return "redirect:/classroom/detail/" + classSn + "/dataroom/" + dto.getPostSn();
    }

    // 자료 수정 폼
    @GetMapping("/detail/{classSn}/dataroom/{postSn}/edit")
    public String dataroomEditForm(@PathVariable Long classSn, @PathVariable Long postSn,
            Model model, Authentication authentication) {
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) return "redirect:/classroom/list";
        InstructorBoardDto editDataroom = instructorBoardService.getClassroomDataroomDetail(postSn, classSn);
        if (editDataroom == null) return "redirect:/classroom/detail/" + classSn + "/dataroom";
        model.addAttribute("classroom", classroom);
        model.addAttribute("editDataroom", editDataroom);
        model.addAttribute("existingFiles",
                editDataroom.getAtchFileId() != null
                        ? fileUploadService.retrieveFilesByGroupId(editDataroom.getAtchFileId().intValue())
                        : Collections.emptyList());
        return "classroom/form-classroom-dataroom";
    }

    // 자료 수정 처리 (기존 그룹에 파일 추가 또는 신규 그룹 생성, 실패 시 신규 파일 롤백)
    @PostMapping("/detail/{classSn}/dataroom/{postSn}/edit")
    public String dataroomEdit(@PathVariable Long classSn, @PathVariable Long postSn,
            @ModelAttribute InstructorBoardDto dto,
            @RequestParam(required = false) List<MultipartFile> attachFiles,
            Authentication authentication, RedirectAttributes redirectAttrs) {
        String userId = authentication.getName();
        if (getOwnedClassroom(classSn, userId) == null) return "redirect:/classroom/list";

        dto.setPostSn(postSn);
        dto.setClassSn(classSn);
        dto.setWrtrUserId(userId);
        dto.setInstrUserId(userId);

        String detailUrl  = "redirect:/classroom/detail/" + classSn + "/dataroom/" + postSn;
        String editErrUrl = "redirect:/classroom/detail/" + classSn + "/dataroom/" + postSn + "/edit?error=fileUploadFailed";

        int newGroupId = -1;
        List<Integer> addedFileSns = Collections.emptyList();

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
                    log.error("자료 수정 파일 업로드 실패 (postSn={}, groupId={})", postSn, existingGroupId, e);
                    fileUploadService.retrieveFilesByGroupId(existingGroupId.intValue())
                            .stream().map(f -> f.getAtchFileDtlSn())
                            .filter(sn -> !before.contains(sn))
                            .forEach(sn -> fileUploadService.removeFile(sn, userId));
                    redirectAttrs.addFlashAttribute("toastMsg", "파일 업로드에 실패했습니다.");
                    redirectAttrs.addFlashAttribute("toastType", "error");
                    return editErrUrl;
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
                    log.error("자료 수정 신규 파일 그룹 생성 실패 (postSn={}, groupId={})", postSn, newGroupId, e);
                    cleanupFileGroup(newGroupId, userId);
                    redirectAttrs.addFlashAttribute("toastMsg", "파일 업로드에 실패했습니다.");
                    redirectAttrs.addFlashAttribute("toastType", "error");
                    return editErrUrl;
                }
            }
        }

        try {
            instructorBoardService.updateClassroomDataroom(dto);
        } catch (Exception e) {
            log.error("자료 수정 DB 반영 실패 (postSn={}, groupId={})", postSn, newGroupId, e);
            addedFileSns.forEach(sn -> fileUploadService.removeFile(sn, userId));
            cleanupFileGroup(newGroupId, userId);
            redirectAttrs.addFlashAttribute("toastMsg", "수정에 실패했습니다.");
            redirectAttrs.addFlashAttribute("toastType", "error");
            return editErrUrl;
        }

        redirectAttrs.addFlashAttribute("toastMsg", "자료가 수정되었습니다.");
        return detailUrl;
    }

    // 첨부파일 단건 삭제 (자료 소유권 검증 후 제거)
    @PostMapping("/detail/{classSn}/dataroom/{postSn}/file/{fileDtlSn}/delete")
    public String dataroomFileDelete(@PathVariable Long classSn, @PathVariable Long postSn,
            @PathVariable Integer fileDtlSn, Authentication authentication, RedirectAttributes redirectAttrs) {
        String editUrl = "redirect:/classroom/detail/" + classSn + "/dataroom/" + postSn + "/edit";
        InstructorBoardDto dataroom = instructorBoardService.getClassroomDataroomDetail(postSn, classSn);
        if (dataroom == null || dataroom.getAtchFileId() == null) return editUrl;

        boolean owned = fileUploadService.retrieveFilesByGroupId(dataroom.getAtchFileId().intValue())
                .stream().anyMatch(f -> fileDtlSn.equals(f.getAtchFileDtlSn()));
        if (!owned) {
            log.warn("파일 소유권 불일치 — 삭제 거부 (postSn={}, fileDtlSn={})", postSn, fileDtlSn);
            return editUrl;
        }
        fileUploadService.removeFile(fileDtlSn, authentication.getName());
        redirectAttrs.addFlashAttribute("toastMsg", "파일이 삭제되었습니다.");
        return editUrl;
    }

    // 자료 삭제 (소프트)
    @PostMapping("/detail/{classSn}/dataroom/{postSn}/delete")
    public String dataroomDelete(@PathVariable Long classSn, @PathVariable Long postSn,
            Authentication authentication, RedirectAttributes redirectAttrs) {
        if (getOwnedClassroom(classSn, authentication.getName()) == null) return "redirect:/classroom/list";
        instructorBoardService.deleteClassroomDataroom(postSn, classSn);
        redirectAttrs.addFlashAttribute("toastMsg", "자료가 삭제되었습니다.");
        return "redirect:/classroom/detail/" + classSn + "/dataroom";
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
