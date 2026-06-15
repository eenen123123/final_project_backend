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
        return cartMapper.selectCartByUserId(userId);
    }

    @Override
    @Transactional
    public void addToCart(CartDto cartDto) {
        if (cartDto.getProdSn() == null || cartDto.getProdDivCd() == null) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }

        if (cartDto.getProdDivCd() == ProductType.TEXTBOOK) {
            TextbookDto textbook = textbookMapper.selectTextbookBySn(cartDto.getProdSn());
            if (textbook == null || !"Y".equals(textbook.getUseYn())) {
                throw new FinalProjectException(ErrorCode.TEXTBOOK_NOT_FOUND);
            }
            cartDto.setProdNm(textbook.getTextbookNm());
            cartDto.setProdPrice(textbook.getSalePrcAmt());
        } else if (cartDto.getProdDivCd() == ProductType.COURSE) {
            CourseDto course = courseMapper.selectCourseBySn(cartDto.getProdSn());
            if (course == null) {
                throw new FinalProjectException(ErrorCode.COURSE_NOT_FOUND);
            }
            cartDto.setProdNm(course.getCourseNm());
            cartDto.setProdPrice(course.getCoursePrice());
        }

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
