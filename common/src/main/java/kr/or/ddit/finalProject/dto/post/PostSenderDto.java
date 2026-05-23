package kr.or.ddit.finalProject.dto.post;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSenderDto implements Serializable {

    private String sndrUserId; // 복합PK(1/2) · MEMBER.USER_ID 참조
    private Long ntceSn; // 복합PK(2/2) · NTCE_MST.NTCE_SN 참조
    private String delYn; // 논리 삭제 · DEFAULT 'N' 권장
    private LocalDateTime delDt;
}