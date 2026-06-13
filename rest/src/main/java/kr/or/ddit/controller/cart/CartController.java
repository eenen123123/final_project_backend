package kr.or.ddit.controller.cart;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.cart.CartDto;
import kr.or.ddit.finalProject.service.cart.CartService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // GET /api/cart
    @GetMapping
    public ResponseEntity<List<CartDto>> getCart(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(cartService.retrieveCart(userId));
    }

    // POST /api/cart
    @PostMapping
    public ResponseEntity<String> addToCart(Authentication authentication,
            @RequestBody CartDto cartDto) {
        cartDto.setUserId(authentication.getName());
        cartService.addToCart(cartDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("장바구니에 담겼습니다.");
    }

    // DELETE /api/cart/{cartSn}
    @DeleteMapping("/{cartSn}")
    public ResponseEntity<Void> removeCartItem(Authentication authentication,
            @PathVariable Long cartSn) {
        cartService.removeCartItem(cartSn, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/cart
    @DeleteMapping
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        cartService.clearCart(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
