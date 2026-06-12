package kr.or.ddit.finalProject.service.instructor;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import kr.or.ddit.finalProject.dto.instructor.InstructorCareerDto;
import kr.or.ddit.finalProject.dto.instructor.InstructorCareerSaveRequest;
import kr.or.ddit.finalProject.dto.instructor.InstructorDetailResponse;
import kr.or.ddit.finalProject.dto.instructor.InstructorDto;
import kr.or.ddit.finalProject.dto.instructor.InstructorFeaturedCourseResponse;
import kr.or.ddit.finalProject.dto.instructor.InstructorListResponse;
import kr.or.ddit.finalProject.dto.instructor.InstructorRecentPostResponse;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.instructor.InstructorBoardMapper;
import kr.or.ddit.finalProject.mapper.instructor.InstructorCareerMapper;
import kr.or.ddit.finalProject.mapper.instructor.InstructorFeaturedCourseMapper;
import kr.or.ddit.finalProject.mapper.instructor.InstructorMapper;
import kr.or.ddit.finalProject.service.file.CloudinaryUploadService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstructorServiceImpl implements InstructorService {

    private final InstructorCareerMapper careerMapper;
    private final InstructorMapper instructorMapper;
    private final InstructorFeaturedCourseMapper featuredCourseMapper;
    private final InstructorBoardMapper instructorBoardMapper;
    private final CloudinaryUploadService cloudinaryUploadService;

    // CAREER_TYPE_CD 허용 값 (01: 약력 / 02: 저서 / 03: 수상 / 04: 방송출연)
    private static final Set<String> VALID_CAREER_TYPE_CDS = Set.of("01", "02", "03", "04");
    // 연도: 4자리 숫자만 허용
    private static final Pattern YEAR_PATTERN = Pattern.compile("\\d{4}");

    // ──────────────────────────────────────────────
    // 공개 강사 조회
    // ──────────────────────────────────────────────

    @Override
    public List<InstructorListResponse> retrieveInstructors(Long subjClId) {
        return instructorMapper.selectInstructors(subjClId);
    }

    @Override
    public Map<String, List<InstructorListResponse>> retrieveInstructorsBySubject() {
        return instructorMapper.selectInstructors(null).stream()
                .collect(Collectors.groupingBy(
                        InstructorListResponse::getSubjectClNm,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    @Override
    public InstructorDetailResponse retrieveInstructorDetail(String instrUuid) {
        return instructorMapper.selectInstructorByUuid(instrUuid);
    }

    @Override
    public List<InstructorFeaturedCourseResponse> retrieveFeaturedCourses(String instrUuid) {
        return featuredCourseMapper.selectFeaturedCourses(instrUuid);
    }

    @Override
    public List<InstructorRecentPostResponse> retrieveRecentPosts(String instrUuid, int size) {
        return instructorBoardMapper.selectRecentPosts(instrUuid, size);
    }

    // ──────────────────────────────────────────────
    // 강사 프로필 관리
    // ──────────────────────────────────────────────

    @Override
    public InstructorDto retrieveProfile(String instrUserId) {
        return careerMapper.selectInstructor(instrUserId);
    }

    @Override
    public List<InstructorCareerDto> retrieveCareers(String instrUserId) {
        // DEL_YN = 'N' 조건은 Mapper XML에서 처리
        return careerMapper.selectCareersByInstructor(instrUserId);
    }

    // ──────────────────────────────────────────────
    // 프로필 이미지 업로드
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public void updateProfileImage(String instrUserId, MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new FinalProjectException(ErrorCode.FILE_EMPTY);
        }

        String imageUrl;
        try {
            // Cloudinary에 업로드하고 CDN URL을 받습니다.
            // 이미지/PDF 외 형식이면 CloudinaryUploadService 내부에서 예외를 던집니다.
            imageUrl = cloudinaryUploadService.uploadFileToCloudinary(imageFile);
        } catch (IOException e) {
            throw new FinalProjectException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        careerMapper.updateProfileImg(instrUserId, imageUrl);
    }

    // ──────────────────────────────────────────────
    // 프로필 이미지 제거
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public void removeProfileImage(String instrUserId) {
        // null을 전달하여 INSTR_PROFILE_IMG 컬럼을 NULL로 업데이트합니다.
        careerMapper.updateProfileImg(instrUserId, null);
    }

    // ──────────────────────────────────────────────
    // 소개글 수정
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public void updateIntro(String instrUserId, String instrIntro) {
        // 빈 문자열로 저장하면 조회 시 공백 처리가 번거로우므로 null로 변환합니다.
        String intro = (instrIntro != null && !instrIntro.isBlank()) ? instrIntro : null;
        careerMapper.updateInstrIntro(instrUserId, intro);
    }

    // ──────────────────────────────────────────────
    // 약력 항목 등록
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public void addCareer(String instrUserId, InstructorCareerSaveRequest request) {
        validateCareerRequest(request);
        InstructorCareerDto dto = new InstructorCareerDto();
        dto.setInstrUserId(instrUserId);
        dto.setCareerTypeCd(request.getCareerTypeCd());
        dto.setCareerStrtYr(request.getCareerStrtYr());
        dto.setCareerCont(request.getCareerCont());
        Integer sortOrd = request.getSortOrd();
        dto.setSortOrd(sortOrd != null ? sortOrd : 0);
        dto.setRgtrId(instrUserId); // 등록자는 로그인한 강사 본인

        // 종료 연도가 빈 문자열로 들어오는 경우 null로 처리
        // (약력(01) 이외 유형이거나, 현재 진행 중인 항목)
        String endYr = request.getCareerEndYr();
        dto.setCareerEndYr((endYr != null && !endYr.isBlank()) ? endYr : null);

        careerMapper.insertCareer(dto);
    }

    // ──────────────────────────────────────────────
    // 약력 항목 수정
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public void modifyCareer(Long careerSn, String instrUserId, InstructorCareerSaveRequest request) {
        validateCareerRequest(request);
        InstructorCareerDto existing = careerMapper.selectCareerBySn(careerSn);

        if (existing == null || "Y".equals(existing.getDelYn())) {
            throw new FinalProjectException(ErrorCode.NOT_FOUND);
        }
        if (!existing.getInstrUserId().equals(instrUserId)) {
            throw new FinalProjectException(ErrorCode.CAREER_ACCESS_DENIED);
        }

        InstructorCareerDto dto = new InstructorCareerDto();
        dto.setCareerSn(careerSn);
        dto.setCareerTypeCd(request.getCareerTypeCd());
        dto.setCareerStrtYr(request.getCareerStrtYr());
        dto.setCareerCont(request.getCareerCont());
        Integer sortOrd = request.getSortOrd();
        dto.setSortOrd(sortOrd != null ? sortOrd : 0);
        dto.setLastMdfrId(instrUserId); // 최종 수정자는 로그인한 강사 본인

        String endYr = request.getCareerEndYr();
        dto.setCareerEndYr((endYr != null && !endYr.isBlank()) ? endYr : null);

        careerMapper.updateCareer(dto);
    }

    // ──────────────────────────────────────────────
    // 약력 항목 소프트 딜리트
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public void removeCareer(Long careerSn, String instrUserId) {
        InstructorCareerDto existing = careerMapper.selectCareerBySn(careerSn);

        if (existing == null || "Y".equals(existing.getDelYn())) {
            throw new FinalProjectException(ErrorCode.NOT_FOUND);
        }
        // 본인 항목인지 확인 — 다른 강사의 약력을 삭제하는 것을 방지
        if (!existing.getInstrUserId().equals(instrUserId)) {
            throw new FinalProjectException(ErrorCode.CAREER_ACCESS_DENIED);
        }

        // 물리 삭제 대신 DEL_YN = 'Y'로 소프트 딜리트 처리
        careerMapper.deleteCareer(careerSn, instrUserId);
    }

    // ──────────────────────────────────────────────
    // 내부 검증
    // ──────────────────────────────────────────────

    /**
     * 약력 저장/수정 요청의 입력값을 검증합니다.
     *
     * <ul>
     *   <li>careerTypeCd: 01·02·03·04 중 하나여야 합니다.</li>
     *   <li>careerStrtYr: 4자리 숫자(예: 2020)여야 합니다.</li>
     *   <li>careerEndYr: 입력된 경우 4자리 숫자이고 시작 연도 이상이어야 합니다.</li>
     * </ul>
     *
     * @throws FinalProjectException BAD_REQUEST — 형식이 잘못된 경우
     */
    private void validateCareerRequest(InstructorCareerSaveRequest request) {
        if (!VALID_CAREER_TYPE_CDS.contains(request.getCareerTypeCd())) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }

        String strtYr = request.getCareerStrtYr();
        if (strtYr == null || !YEAR_PATTERN.matcher(strtYr).matches()) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }

        String endYr = request.getCareerEndYr();
        if (endYr != null && !endYr.isBlank()) {
            if (!YEAR_PATTERN.matcher(endYr).matches()) {
                throw new FinalProjectException(ErrorCode.BAD_REQUEST);
            }
            // 종료 연도는 시작 연도보다 앞설 수 없음
            if (Integer.parseInt(endYr) < Integer.parseInt(strtYr)) {
                throw new FinalProjectException(ErrorCode.BAD_REQUEST);
            }
        }
    }
}
