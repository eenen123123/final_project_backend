package kr.or.ddit.finalProject.mapper.cart;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.cart.CartDto;
import kr.or.ddit.finalProject.dto.cart.ProductType;

@Mapper
public interface CartMapper {

    List<CartDto> selectCartByUserId(@Param("userId") String userId);

    int insertCart(CartDto cartDto);

    int deleteCartBySn(@Param("cartSn") Long cartSn, @Param("userId") String userId);

    int deleteCartByUserId(@Param("userId") String userId);

    // 동일 상품 중복 체크
    CartDto selectCartByUserAndProd(@Param("userId") String userId,
            @Param("prodDivCd") ProductType prodDivCd,
            @Param("prodSn") Long prodSn);

    // 결제 완료 시 구매한 상품을 장바구니에서 제거
    int deleteCartByUserAndProd(@Param("userId") String userId,
            @Param("prodDivCd") ProductType prodDivCd,
            @Param("prodSn") Long prodSn);

    // 교재 수량 변경 (TEXTBOOK만 허용, COURSE는 0 반환)
    int updateCartQty(@Param("cartSn") Long cartSn, @Param("userId") String userId, @Param("itemQty") int itemQty);
}
