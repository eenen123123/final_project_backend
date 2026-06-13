package kr.or.ddit.finalProject.service.cart;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.cart.CartDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
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
    public void addToCart(CartDto cartDto) {
        CartDto existing = cartMapper.selectCartByUserAndProd(cartDto.getUserId(),
                cartDto.getProdDivCd(), cartDto.getProdSn());
        if (existing != null) {
            throw new FinalProjectException(ErrorCode.CART_ITEM_ALREADY_EXISTS);
        }
        cartMapper.insertCart(cartDto);
    }

    @Override
    @Transactional
    public void removeCartItem(Long cartSn, String userId) {
        int deleted = cartMapper.deleteCartBySn(cartSn, userId);
        if (deleted == 0) {
            throw new FinalProjectException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void clearCart(String userId) {
        cartMapper.deleteCartByUserId(userId);
    }
}
