package kr.or.ddit.finalProject.dto.coupon;

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
public class PointHistDto {

    private Long pointHistSn;          // PK
    private Long mcpntSn;              // FK → MEMBER_COUPONPOINT.MCPNT_SN
    private String userId;             // FK → MEMBER.USER_ID
    private PointHistType histType;    // EARN | USE | EXPIRE
    private Long changeAmt;            // 변동량 (양수: 적립, 음수: 사용/소멸)
    private Long ordSn;                // FK → ORDERS.ORD_SN (사용 시)
    private String memo;               // 변동 사유
    private LocalDateTime regDt;       // 등록일시
    private LocalDate expiryDt;        // 만료일 (JOIN from MEMBER_COUPONPOINT)
    private AssetType assetType;       // HM_POINT / STUDY_POINT / HM_MONEY (USE 이력 타입 구분용)
}
