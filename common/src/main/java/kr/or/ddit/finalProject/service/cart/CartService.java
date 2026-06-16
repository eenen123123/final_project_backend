package kr.or.ddit.finalProject.service.cart;

import java.util.List;

import kr.or.ddit.finalProject.dto.cart.CartDto;

public interface CartService {

    List<CartDto> retrieveCart(String userId);

    void addToCart(CartDto cartDto);

    void removeCartItem(Long cartSn, String userId);

    void clearCart(String userId);

    void updateCartQty(Long cartSn, String userId, int itemQty);
}
