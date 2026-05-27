package kr.or.ddit.finalProject.dto.approval;

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
public class ApprovalLineDto implements Serializable {

    private Long aprvlLineSn; // 복합PK(1/2) · 자동증가
    private Long aprvlDocSn; // 복합PK(2/2)
    private Long aprvlOrdr; // 결재자 순서 (1부터 시작)
    private String aprvrUserId; // MEMBER.USER_ID 참조
    private LocalDateTime aprvlDt;
    private String aprvlPrgrsCd; // COM_CD 공통코드 참조
    private String aprvlRsnCn; // 승인·반려 사유 메모
}
