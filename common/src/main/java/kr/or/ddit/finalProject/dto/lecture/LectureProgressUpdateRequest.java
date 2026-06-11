package kr.or.ddit.finalProject.dto.lecture;

public record LectureProgressUpdateRequest(
        Long lectureId,
        Long courseId,
        Integer progress) {

}
