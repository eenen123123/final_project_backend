package kr.or.ddit.finalProject.dto.post;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostMasterDto implements Serializable {

    private Long ntceSn; // 기본키(PK) · 자동증가
    private String ntceSj; // 쪽지 제목
    private String ntceCn; // 쪽지 내용
    private LocalDateTime ntceSndDt; // 쪽지 발송 일시
    private PostTypeEnum ntceTypeCd; // 쪽지 유형
    private String sndrUserId; // 발신자 아이디 (목록 조회 시 JOIN)
}
