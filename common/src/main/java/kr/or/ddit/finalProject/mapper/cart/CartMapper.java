package kr.or.ddit.finalProject.mapper.cart;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.cart.CartDto;

@Mapper
public interface CartMapper {

    List<CartDto> selectCartByUserId(@Param("userId") String userId);

    int insertCart(CartDto cartDto);

    int deleteCartBySn(@Param("cartSn") Long cartSn, @Param("userId") String userId);

    int deleteCartByUserId(@Param("userId") String userId);

    // 동일 상품 중복 체크
    CartDto selectCartByUserAndProd(@Param("userId") String userId,
                                    @Param("prodDivCd") String prodDivCd,
                                    @Param("prodSn") Long prodSn);
}
