package kr.or.ddit.finalProject.dto.course;

import lombok.Data;

/**
 * 커리큘럼에 강좌를 추가할 때 사용하는 요청 DTO.
 * 매핑할 강좌의 일련번호만 받는다.
 */
@Data
public class CourseMappingRequest {
    /** 커리큘럼에 추가할 강좌 일련번호 */
    private Long courseSn;
}
