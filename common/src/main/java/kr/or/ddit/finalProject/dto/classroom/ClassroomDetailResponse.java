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
    private String classStatCd;  // 01=모집중, 02=운영중, 03=종료, 04=대기
    private String enrlStrtYmd;  // 운영 시작일 (YYYY.MM.DD, DB는 YYYYMMDD → 서비스에서 포맷)
    private String enrlEndYmd;   // 운영 종료일 (YYYY.MM.DD, null=무기한)

    private List<ClassroomMemberListResponse> members; // 서비스에서 세팅
    private List<CourseCohortListResponse> cohorts;    // 서비스에서 세팅

    private String instrNm;      // MEMBER.USER_NAME (강사명)
    private int memberCount;     // 현재 수강생 수
    private String instrUuid;    // 강사 프로필 이미지 UUID

}
