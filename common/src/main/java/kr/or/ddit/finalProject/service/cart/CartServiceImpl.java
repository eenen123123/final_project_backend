package kr.or.ddit.finalProject.service.cart;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.cart.CartDto;
import kr.or.ddit.finalProject.mapper.cart.CartMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;

    @Override
    public List<CartDto> retrieveCart(String userId) {
        return cartMapper.selectCartByUserId(userId);
    }

    @Override
    @Transactional
    public boolean addToCart(CartDto cartDto) {
        CartDto existing = cartMapper.selectCartByUserAndProd(
                cartDto.getUserId(), cartDto.getProdDivCd(), cartDto.getProdSn());
        if (existing != null) {
            return true; // 이미 담긴 상품
        }
        cartMapper.insertCart(cartDto);
        return false;
    }

    @Override
    @Transactional
    public void removeCartItem(Long cartSn, String userId) {
        cartMapper.deleteCartBySn(cartSn, userId);
    }

    @Override
    @Transactional
    public void clearCart(String userId) {
        cartMapper.deleteCartByUserId(userId);
    }
}
