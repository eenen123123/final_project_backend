package kr.or.ddit.finalProject.dto.classroom;

import java.util.List;

import kr.or.ddit.finalProject.dto.coursecohort.CourseCohortListResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomDetailResponse {

    private Long classSn;
    private String classNm;
    private String courseNm;
    private Long courseSn;
    private String classStatCd; // 01=모집중, 02=운영중, 03=종료, 04=대기
    private String enrlStrtYmd; // YYYYMMDD → 서비스에서 YYYY.MM.DD 포맷
    private String enrlEndYmd; // YYYYMMDD → 서비스에서 YYYY.MM.DD 포맷 (null=무기한)

    private List<ClassroomMemberListResponse> members; // 서비스에서 세팅
    private List<CourseCohortListResponse> cohorts; // 서비스에서 세팅

    // 임시 데이터
    private String instrNm;
    private int memberCount;

    private String instrUuid;

}
