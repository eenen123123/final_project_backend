package kr.or.ddit.finalProject.dto.featured;

import java.io.Serializable;
import java.time.LocalDateTime;

import kr.or.ddit.finalProject.dto.cart.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeaturedItemDto implements Serializable {

    // FEATURED_ITEM 테이블 컬럼
    private Long featuredSn;        // PK
    private ProductType prodType;   // 상품 유형 (COURSE / TEXTBOOK)
    private Long prodSn;            // 강좌 or 교재 PK
    private String customImg;       // 관리자 등록 노출 이미지 URL
    private String customDesc;      // 관리자 입력 노출 설명
    private Integer sortOrd;        // 카드 노출 순서
    private String rgtrId;          // 등록자 ID
    private LocalDateTime regDt;    // 등록일시

    // JOIN으로 가져오는 원본 데이터 (조회 시 사용)
    private String name;            // 강좌명 or 교재명
    private Long price;             // 강좌 가격 or 판매가
    private String originImg;       // 원본 썸네일 (customImg 없을 때 fallback)
    private String originDesc;      // 원본 설명 (customDesc 없을 때 fallback)
    private String instrUuid;       // 강좌용 강사 UUID (프론트 라우팅에 필요)
}
