package kr.or.ddit.finalProject.mapper.instructor;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.instructor.profile.InstructorCareerDto;
import kr.or.ddit.finalProject.dto.instructor.profile.InstructorDto;

/**
 * 강사 약력 항목 Mapper
 *
 * INSTRUCTOR_CAREER 테이블의 CRUD와 INSTRUCTOR 테이블의
 * 프로필 이미지·소개글 업데이트를 담당합니다.
 * 소유권 확인(instrUserId 일치 여부)은 서비스 레이어에서 처리하므로
 * 이 Mapper는 순수 DB 조작만 담당합니다.
 */
@Mapper
public interface InstructorCareerMapper {

    /**
     * 강사 기본 프로필 조회 (소개글 + 프로필 이미지 URL)
     *
     * @param instrUserId 조회할 강사 ID
     */
    InstructorDto selectInstructor(@Param("instrUserId") String instrUserId);

    /**
     * 특정 강사의 약력 항목 전체 조회
     * DEL_YN = 'N'인 항목만 조회합니다 (소프트 딜리트 적용).
     * 유형(careerTypeCd) → 순서(sortOrd) → 시작연도(careerStrtYr) 순 정렬.
     *
     * @param instrUserId 조회할 강사 ID
     */
    List<InstructorCareerDto> selectCareersByInstructor(@Param("instrUserId") String instrUserId);

    /**
     * 약력 항목 단건 조회
     * 소유권 확인 및 수정/삭제 전 존재 여부 체크에 사용합니다.
     * 소프트 딜리트된 항목도 조회됩니다 (서비스에서 DEL_YN 판단).
     *
     * @param careerSn 조회할 약력 일련번호
     */
    InstructorCareerDto selectCareerBySn(@Param("careerSn") Long careerSn);

    /**
     * 약력 항목 등록
     * INSERT 후 생성된 CAREER_SN이 dto.careerSn에 자동으로 채워집니다 (useGeneratedKeys).
     * RGTR_ID(등록자)와 REG_DT(등록일시)를 함께 저장합니다.
     *
     * @param dto 저장할 약력 항목 (instrUserId, rgtrId 포함 필수)
     */
    void insertCareer(InstructorCareerDto dto);

    /**
     * 약력 항목 수정 (내용·연도·순서 변경 가능)
     * LAST_MDFR_ID(수정자)와 MDFCN_DT(수정일시)를 함께 갱신합니다.
     * 소유권 확인은 서비스에서 합니다.
     *
     * @param dto 수정할 내용 (careerSn, lastMdfrId 포함 필수)
     */
    void updateCareer(InstructorCareerDto dto);

    /**
     * 약력 항목 소프트 딜리트
     * 물리 삭제 대신 DEL_YN = 'Y'로 마킹하고 DEL_DT / DEL_USER_ID를 기록합니다.
     * 소유권 확인은 서비스에서 합니다.
     *
     * @param careerSn  삭제할 약력 일련번호
     * @param delUserId 삭제 처리자 ID
     */
    void deleteCareer(@Param("careerSn") Long careerSn,
                      @Param("delUserId") String delUserId);

    /**
     * 강사 프로필 이미지 URL 업데이트
     * Cloudinary 업로드 후 반환된 URL을 INSTRUCTOR.INSTR_PROFILE_IMG에 저장합니다.
     * null을 전달하면 이미지를 제거합니다.
     *
     * @param instrUserId   대상 강사 ID
     * @param profileImgUrl Cloudinary CDN URL (null이면 이미지 제거)
     */
    void updateProfileImg(@Param("instrUserId") String instrUserId,
                          @Param("profileImgUrl") String profileImgUrl);

    /**
     * 강사 소개글 업데이트
     * INSTRUCTOR.INSTR_INTRO를 수정합니다.
     * 빈 문자열은 서비스에서 null로 변환하여 전달합니다.
     *
     * @param instrUserId 대상 강사 ID
     * @param instrIntro  수정할 소개글 (null이면 소개글 삭제)
     */
    void updateInstrIntro(@Param("instrUserId") String instrUserId,
                          @Param("instrIntro") String instrIntro);
}
