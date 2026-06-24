package kr.or.ddit.finalProject.dto.curriculum;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 커리큘럼 정보 DTO. CURRICULUM 테이블과 매핑되며 강좌 배정 및 기간 관리에 사용된다. */
@EqualsAndHashCode(of = "curriculumId")
@Data
public class CurriculumDto {

    /** PK. SEQ_CURRICULUM.NEXTVAL */
    private Long curriculumId;
    /** 커리큘럼명 (필수, 최대 200자) */
    @NotBlank(message = "커리큘럼명은 필수 입력 항목입니다.")
    @Size(max = 200, message = "커리큘럼명은 200자 이내로 입력해 주세요.")
    private String title;
    /** 담당 강사 ID (MEMBER.USER_ID) */
    private String instructorId;
    /** 사용 여부. 'Y' = 정상, 'N' = 논리 삭제 */
    private String useYn;
    /** 커리큘럼 시작일 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate strtDt;
    /** 커리큘럼 종료일 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDt;
    /** 커리큘럼 설명 (선택, 최대 4000자) */
    @Size(max = 4000, message = "설명은 4000자 이내로 입력해 주세요.")
    private String explnCn;
    /** 최초 등록자 ID */
    private String rgtrId;
    /** 최초 등록일시 */
    private LocalDateTime regDt;
    /** 최종 수정자 ID */
    private String lastMdfrId;
    /** 최종 수정일시 */
    private LocalDateTime mdfcnDt;
    /** 배정된 강좌 수. DB 컬럼 아님 — selectList 서브쿼리로 채워짐 */
    private int courseCount;

}
