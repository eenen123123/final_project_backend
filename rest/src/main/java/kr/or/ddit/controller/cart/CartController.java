package kr.or.ddit.controller.cart;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.cart.CartDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.service.cart.CartService;
import kr.or.ddit.finalProject.service.member.MemberService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final MemberService memberService;

    // GET /api/cart
    @GetMapping
    public ResponseEntity<List<CartDto>> getCart(
            @RequestHeader("Authorization") String authHeader) {
        String userId = getuserId(authHeader);
        return ResponseEntity.ok(cartService.retrieveCart(userId));
    }

    // POST /api/cart
    @PostMapping
    public ResponseEntity<String> addToCart(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CartDto cartDto) {
        String userId = getuserId(authHeader);
        cartDto.setUserId(userId);
        boolean duplicate = cartService.addToCart(cartDto);
        if (duplicate) {
            return ResponseEntity.ok("이미 장바구니에 담긴 상품입니다.");
        }
        return ResponseEntity.ok("장바구니에 담겼습니다.");
    }

    // DELETE /api/cart/{cartSn}
    @DeleteMapping("/{cartSn}")
    public ResponseEntity<Void> removeCartItem(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long cartSn) {
        String userId = getuserId(authHeader);
        cartService.removeCartItem(cartSn, userId);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/cart
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @RequestHeader("Authorization") String authHeader) {
        String userId = getuserId(authHeader);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    private String getuserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        MemberDto member = memberService.getMemberByToken(token);
        return member.getUserId();
    }
}
