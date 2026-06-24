package kr.or.ddit.finalProject.dto.order;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderSearchCondition {
    private LocalDateTime from;
    private LocalDateTime to;

    // 관리자 검색용
    private String userId;       // 주문자 ID 검색
    private String keyword;      // 주문명 검색
    private String ordStatCd;    // 주문 상태 필터
}
