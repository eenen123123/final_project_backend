package kr.or.ddit.controller.instructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.file.FileCtxType;
import kr.or.ddit.finalProject.dto.file.FileDto;
import kr.or.ddit.finalProject.service.course.CourseService;
import kr.or.ddit.finalProject.service.file.FileUploadService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/instructor/course/materials")
@RequiredArgsConstructor
public class InstructorCourseMaterialsController {

    private final CourseService courseService;
    private final FileUploadService fileUploadService;

    @GetMapping
    public String materialsPage(Model model, Authentication authentication) {
        String userId = authentication.getName();
        List<CourseDto> courseList = courseService.retrieveCoursesByInstructor(userId);
        model.addAttribute("courseList", courseList);
        return "admin:/instructor/courseMaterials";
    }

    @GetMapping("/{courseSn}/files")
    @ResponseBody
    public ResponseEntity<List<FileDto>> getFiles(@PathVariable Long courseSn,
            Authentication authentication) {
        String userId = authentication.getName();
        CourseDto course = courseService.retrieveCourseBySn(courseSn);
        if (course == null || !userId.equals(course.getInstrUserId())) {
            return ResponseEntity.status(403).build();
        }
        if (course.getAtchFileId() == null || course.getAtchFileId().isBlank()) {
            return ResponseEntity.ok(List.of());
        }
        List<FileDto> files =
                fileUploadService.retrieveFilesByGroupId(Integer.parseInt(course.getAtchFileId()));
        return ResponseEntity.ok(files);
    }

    @PostMapping("/{courseSn}/files")
    @ResponseBody
    public ResponseEntity<FileDto> uploadFile(@PathVariable Long courseSn,
            @RequestParam MultipartFile file, Authentication authentication) {
        String userId = authentication.getName();

        CourseDto course = courseService.retrieveCourseBySn(courseSn);
        if (course == null || !userId.equals(course.getInstrUserId())) {
            return ResponseEntity.status(403).build();
        }

        int groupId;
        if (course.getAtchFileId() == null || course.getAtchFileId().isBlank()) {
            groupId = fileUploadService.createFileGroup();
            courseService.updateCourseAtchFileId(courseSn, String.valueOf(groupId));
        } else {
            groupId = Integer.parseInt(course.getAtchFileId());
        }

        // 임의로 FileCtxId를 courseSn으로 설정했습니다 
        FileDto result = fileUploadService.uploadFile(file, userId, groupId, FileCtxType.COURSE,
                String.valueOf(courseSn));
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/files/{atchFileDtlSn}")
    @ResponseBody
    public ResponseEntity<FileDto> deleteFile(@PathVariable Integer atchFileDtlSn,
            @RequestParam Long courseSn, Authentication authentication) {
        String userId = authentication.getName();
        CourseDto course = courseService.retrieveCourseBySn(courseSn);
        if (course == null || !userId.equals(course.getInstrUserId())) {
            return ResponseEntity.status(403).build();
        }
        fileUploadService.removeFile(atchFileDtlSn, userId);
        return ResponseEntity.ok().build();
    }

}
