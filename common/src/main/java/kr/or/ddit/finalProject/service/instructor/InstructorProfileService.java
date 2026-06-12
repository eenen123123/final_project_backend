package kr.or.ddit.finalProject.service.instructor;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import kr.or.ddit.finalProject.dto.instructor.InstructorCareerDto;
import kr.or.ddit.finalProject.dto.instructor.InstructorCareerSaveRequest;
import kr.or.ddit.finalProject.dto.instructor.InstructorDetailResponse;
import kr.or.ddit.finalProject.dto.instructor.InstructorDto;
import kr.or.ddit.finalProject.dto.instructor.InstructorFeaturedCourseResponse;
import kr.or.ddit.finalProject.dto.instructor.InstructorListResponse;
import kr.or.ddit.finalProject.dto.instructor.InstructorRecentPostResponse;

/**
 * 강사 개인 페이지 관리 서비스
 *
 * [담당 기능] - 강사 기본 프로필 조회 (소개글, 프로필 이미지 URL) - 프로필 이미지 업로드 (Cloudinary →
 * INSTRUCTOR.INSTR_PROFILE_IMG) - 프로필 이미지 제거 (INSTRUCTOR.INSTR_PROFILE_IMG =
 * NULL) - 소개글 수정 (INSTRUCTOR.INSTR_INTRO, 빈 문자열 → null 처리) - 약력 항목 조회 / 등록 / 수정
 * / 소프트 딜리트 (INSTRUCTOR_CAREER)
 *
 * [관련 URL] GET /instructor/profile/instructor POST
 * /instructor/profile/instructor/image POST
 * /instructor/profile/instructor/image/delete POST
 * /instructor/profile/instructor/intro POST
 * /instructor/profile/instructor/careers POST
 * /instructor/profile/instructor/careers/{sn}/update POST
 * /instructor/profile/instructor/careers/{sn}/delete
 */
public interface InstructorProfileService {

    // ── 공개 강사 조회 ──────────────────────────────────────────────

    /** 과목 분류 ID로 강사 목록 조회 (null이면 전체) */
    List<InstructorListResponse> retrieveInstructors(Long subjClId);

    /** 과목 분류별로 그룹핑된 강사 목록 조회 */
    Map<String, List<InstructorListResponse>> retrieveInstructorsBySubject();

    /** UUID로 강사 공개 상세 정보 조회 */
    InstructorDetailResponse retrieveInstructorDetail(String instrUuid);

    /** 강사의 추천 강좌 목록 조회 */
    List<InstructorFeaturedCourseResponse> retrieveFeaturedCourses(String instrUuid);

    /** 강사의 최근 게시글 목록 조회 */
    List<InstructorRecentPostResponse> retrieveRecentPosts(String instrUuid, int size);

    // ── 강사 프로필 관리 ────────────────────────────────────────────

    /**
     * 강사 기본 프로필 조회 소개글(instrIntro)과 프로필 이미지 URL(instrProfileImg)을 반환합니다.
     *
     * @param instrUserId 조회할 강사 ID
     * @return InstructorDto (instrIntro, instrProfileImg 포함)
     */
    InstructorDto retrieveProfile(String instrUserId);

    /**
     * 강사의 약력 항목 전체 조회 DEL_YN = 'N'인 항목만 반환합니다 (소프트 딜리트 적용). 유형(careerTypeCd) →
     * 순서(sortOrd) → 시작연도(careerStrtYr) 순으로 반환됩니다.
     *
     * @param instrUserId 조회할 강사 ID
     * @return 약력 항목 목록
     */
    List<InstructorCareerDto> retrieveCareers(String instrUserId);

    /**
     * 프로필 이미지 업로드 및 저장 Cloudinary에 이미지를 업로드하고, 반환된 URL을 INSTRUCTOR 테이블에 저장합니다.
     *
     * @param instrUserId 대상 강사 ID
     * @param imageFile 업로드할 이미지 파일 (이미지 형식만 허용)
     */
    void updateProfileImage(String instrUserId, MultipartFile imageFile);

    /**
     * 프로필 이미지 제거 INSTRUCTOR.INSTR_PROFILE_IMG를 NULL로 업데이트합니다.
     *
     * @param instrUserId 대상 강사 ID
     */
    void removeProfileImage(String instrUserId);

    /**
     * 강사 소개글 수정 빈 문자열로 들어오면 null로 변환하여 저장합니다 (소개글 삭제 효과).
     *
     * @param instrUserId 대상 강사 ID
     * @param instrIntro 수정할 소개글 내용 (빈 문자열 허용)
     */
    void updateIntro(String instrUserId, String instrIntro);

    /**
     * 약력 항목 등록 careerEndYr가 빈 문자열로 들어오는 경우 null로 변환하여 저장합니다. RGTR_ID(등록자)를
     * instrUserId로 설정합니다.
     *
     * @param instrUserId 강사 ID (컨트롤러에서 로그인 사용자 ID로 주입)
     * @param request 저장할 약력 항목 데이터
     */
    void addCareer(String instrUserId, InstructorCareerSaveRequest request);

    /**
     * 약력 항목 수정 소유권 확인 후 수정합니다 (다른 강사의 항목 수정 불가). LAST_MDFR_ID(수정자)를
     * instrUserId로 설정합니다.
     *
     * @param careerSn 수정할 약력 항목 일련번호
     * @param instrUserId 요청한 강사 ID (소유권 확인 + 수정자 기록용)
     * @param request 수정할 내용
     */
    void modifyCareer(Long careerSn, String instrUserId, InstructorCareerSaveRequest request);

    /**
     * 약력 항목 소프트 딜리트 소유권 확인 후 DEL_YN = 'Y'로 마킹합니다 (물리 삭제 없음).
     *
     * @param careerSn 삭제할 약력 항목 일련번호
     * @param instrUserId 요청한 강사 ID (소유권 확인 + 삭제자 기록용)
     */
    void removeCareer(Long careerSn, String instrUserId);
}
