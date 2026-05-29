package kr.or.ddit.finalProject.dto.classroom;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomListResponse {

    private Long classSn;
    private String classNm;
    private String courseNm;
    private String classStatCd; // 01=모집중, 02=운영중, 03=종료, 04=대기
    private int studentCount;   // 수강중(01) 학생 수
    private LocalDateTime regDt;

}
