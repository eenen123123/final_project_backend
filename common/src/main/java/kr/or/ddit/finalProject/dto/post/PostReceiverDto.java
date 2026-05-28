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
public class PostReceiverDto implements Serializable {

    private String rcvrUserId; // 복합PK(1/2) · MEMBER.USER_ID 참조
    private Long ntceSn; // 복합PK(2/2) · NTCE_MST.NTCE_SN 참조
    private String ntceCfmtnYn; // DEFAULT 'N' 권장
    private LocalDateTime ntceCfmtnDt; // 수신자가 읽은 일시 (CFMTN_YN='Y' 시 기록)
    private String delYn; // 논리 삭제 · DEFAULT 'N'
    private LocalDateTime delDt;
}
