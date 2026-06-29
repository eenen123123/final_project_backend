package kr.or.ddit.finalProject.dto.instructor.profile;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorDto implements Serializable {

    /** 기본키(PK) */
    private String instrUserId;

    /** 강사 공개 UUID (INSTRUCTOR.INSTR_UUID) */
    private String instrUuid;

    /** 강사 소개글 (INSTRUCTOR.INSTR_INTRO) */
    private String instrIntro;

    /** 강사 공개 프로필 이미지 URL (Cloudinary CDN, INSTRUCTOR.INSTR_PROFILE_IMG) */
    private String instrProfileImg;
}
