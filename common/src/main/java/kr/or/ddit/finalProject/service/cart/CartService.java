package kr.or.ddit.finalProject.service.cart;

import java.util.List;

import kr.or.ddit.finalProject.dto.cart.CartDto;

public interface CartService {

    List<CartDto> retrieveCart(String userId);

    // 담기 — 이미 있으면 true 반환(중복), 없으면 insert 후 false 반환
    boolean addToCart(CartDto cartDto);

    void removeCartItem(Long cartSn, String userId);

    void clearCart(String userId);
}
