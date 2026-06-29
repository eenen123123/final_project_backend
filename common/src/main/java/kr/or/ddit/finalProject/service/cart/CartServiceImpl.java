package kr.or.ddit.finalProject.service.cart;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.cart.CartDto;
import kr.or.ddit.finalProject.dto.cart.ProductType;
import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.textbook.TextbookDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.cart.CartMapper;
import kr.or.ddit.finalProject.mapper.course.CourseMapper;
import kr.or.ddit.finalProject.mapper.textbook.TextbookMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final TextbookMapper textbookMapper;
    private final CourseMapper courseMapper;

    @Override
    public List<CartDto> retrieveCart(String userId) {
        cartMapper.deleteExpiredCartItems(userId);
        return cartMapper.selectCartByUserId(userId);
    }

    @Override
    @Transactional
    public void addToCart(CartDto cartDto) {
        // 상품 SN, 상품 구분 코드 누락 여부
        if (cartDto.getProdSn() == null || cartDto.getProdDivCd() == null) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }

        if (cartDto.getProdDivCd() == ProductType.TEXTBOOK) {
            // 교재 존재 여부 및 판매 중(USE_YN=Y) 여부
            TextbookDto textbook = textbookMapper.selectTextbookBySn(cartDto.getProdSn());
            if (textbook == null || !"Y".equals(textbook.getUseYn())) {
                throw new FinalProjectException(ErrorCode.TEXTBOOK_NOT_FOUND);
            }
            // 상품명/가격은 DB 조회값으로 덮어씀 (클라이언트 값 무시)
            cartDto.setProdNm(textbook.getTextbookNm());
            cartDto.setProdPrice(textbook.getSalePrcAmt());
        } else if (cartDto.getProdDivCd() == ProductType.COURSE) {
            // 강좌 존재 여부
            CourseDto course = courseMapper.selectCourseBySn(cartDto.getProdSn());
            if (course == null) {
                throw new FinalProjectException(ErrorCode.COURSE_NOT_FOUND);
            }
            // 상품명/가격은 DB 조회값으로 덮어씀 (클라이언트 값 무시)
            cartDto.setProdNm(course.getCourseNm());
            cartDto.setProdPrice(course.getCoursePrice());
            // 강좌 수량은 항상 1, null이면 1로 세팅, 1 이외의 값은 거부
            if (cartDto.getItemQty() == null) {
                cartDto.setItemQty(1);
            } else if (cartDto.getItemQty() != 1) {
                throw new FinalProjectException(ErrorCode.BAD_REQUEST);
            }
        }

        // 동일 상품이 이미 장바구니에 담겨있는지 중복 체크
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
        // 본인 항목인지 확인 (deleted=0이면 없거나 타인 항목)
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

    @Override
    @Transactional
    public void updateCartQty(Long cartSn, String userId, int itemQty) {
        // 수량 최솟값(1) 체크
        if (itemQty < 1) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
        // updated=0이면 강좌(COURSE)이거나 본인 항목이 아닌 경우
        int updated = cartMapper.updateCartQty(cartSn, userId, itemQty);
        if (updated == 0) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
    }
}
