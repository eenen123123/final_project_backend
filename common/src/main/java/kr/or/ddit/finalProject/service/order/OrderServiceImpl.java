package kr.or.ddit.finalProject.service.order;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.order.OrderDto;
import kr.or.ddit.finalProject.dto.order.OrderItemDto;
import kr.or.ddit.finalProject.dto.order.OrderStatus;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.order.OrderMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderDto createOrder(String userId, List<OrderItemDto> items) {
        if (items == null || items.isEmpty()) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }

        long totAmt = 0;
        List<OrderItemDto> orderItems = new ArrayList<>();
        for (OrderItemDto req : items) {
            OrderItemDto item = lookupProduct(req);
            totAmt += item.getProdPrice() * item.getItemQty();
            orderItems.add(item);
        }

        String ordNm = orderItems.get(0).getProdNm();
        if (orderItems.size() > 1) {
            ordNm += " 외 " + (orderItems.size() - 1) + "건";
        }

        OrderDto order = OrderDto.builder()
                .ordId(UUID.randomUUID().toString())
                .userId(userId)
                .ordNm(ordNm)
                .totAmt(totAmt)
                .ordStatCd(OrderStatus.PENDING)
                .build();
        orderMapper.insertOrder(order);

        for (OrderItemDto item : orderItems) {
            item.setOrdSn(order.getOrdSn());
            orderMapper.insertOrderItem(item);
        }
        order.setItems(orderItems);
        return order;
    }

    // 상품명/가격을 DB에서 조회해 주문 상품 스냅샷 생성
    private OrderItemDto lookupProduct(OrderItemDto req) {
        if (req.getProdDivCd() == null || req.getProdSn() == null) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }

        OrderItemDto found;
        switch (req.getProdDivCd()) {
            case COURSE:
                found = orderMapper.selectCourseForOrder(req.getProdSn());
                if (found == null) {
                    throw new FinalProjectException(ErrorCode.COURSE_NOT_FOUND);
                }
                break;
            case TEXTBOOK:
                found = orderMapper.selectTextbookForOrder(req.getProdSn());
                if (found == null) {
                    throw new FinalProjectException(ErrorCode.TEXTBOOK_NOT_FOUND);
                }
                break;
            default:
                throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }

        int qty = (req.getItemQty() == null || req.getItemQty() < 1) ? 1 : req.getItemQty();
        return OrderItemDto.builder()
                .prodDivCd(req.getProdDivCd())
                .prodSn(req.getProdSn())
                .prodNm(found.getProdNm())
                .prodPrice(found.getProdPrice())
                .itemQty(qty)
                .build();
    }
}
