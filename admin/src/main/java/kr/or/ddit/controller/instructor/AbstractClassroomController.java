package kr.or.ddit.controller.instructor;

import kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse;
import kr.or.ddit.finalProject.service.assignment.AssignmentBoardService;
import kr.or.ddit.finalProject.service.classroom.ClassroomService;
import kr.or.ddit.finalProject.service.exam.ExamService;
import kr.or.ddit.finalProject.service.instructor.InstructorBoardService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 클래스룸 관련 컨트롤러 공통 기반 클래스.
 * 소유권 확인(getOwnedClassroom)과 탭 배지 모델 주입(addTabBadges)을 제공한다.
 */
public abstract class AbstractClassroomController {

    protected final ClassroomService classroomService;
    protected final AssignmentBoardService assignmentBoardService;
    protected final InstructorBoardService instructorBoardService;
    protected final ExamService examService;

    protected AbstractClassroomController(ClassroomService classroomService,
                                          AssignmentBoardService assignmentBoardService,
                                          InstructorBoardService instructorBoardService,
                                          ExamService examService) {
        this.classroomService = classroomService;
        this.assignmentBoardService = assignmentBoardService;
        this.instructorBoardService = instructorBoardService;
        this.examService = examService;
    }

    @ModelAttribute
    public void addTabBadges(@PathVariable(required = false) Long classSn, Model model) {
        if (classSn != null) {
            model.addAttribute("assignmentCount",
                    assignmentBoardService.getPendingGradeCount(classSn));
            model.addAttribute("unansweredQnaCount",
                    instructorBoardService.getUnansweredQnaCount(classSn));
            model.addAttribute("pendingExamGradeCount",
                    examService.countPendingGradesByClassSn(classSn));
        }
    }

    protected ClassroomDetailResponse getOwnedClassroom(Long classSn, String userId) {
        try {
            ClassroomDetailResponse classroom = classroomService.retrieveClassroomDetail(classSn);
            return userId.equals(classroom.getInstrUserId()) ? classroom : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
