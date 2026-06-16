package kr.or.ddit.finalProject.service.order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.cart.ProductType;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.order.OrderDto;
import kr.or.ddit.finalProject.dto.order.OrderItemDto;
import kr.or.ddit.finalProject.dto.order.OrderSearchCondition;
import kr.or.ddit.finalProject.dto.order.OrderStatus;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.order.OrderMapper;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final long BOOK_SHIPPING_FEE = 3000; // 교재 포함 시 배송비

    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderDto createOrder(String userId, List<OrderItemDto> items) {
        if (items == null || items.isEmpty()) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }

        long totAmt = 0;
        boolean hasBook = false;
        List<OrderItemDto> orderItems = new ArrayList<>();
        for (OrderItemDto req : items) {
            OrderItemDto item = lookupProduct(req);
            totAmt += item.getProdPrice() * item.getItemQty();
            if (item.getProdDivCd() == ProductType.TEXTBOOK) {
                hasBook = true;
            }
            orderItems.add(item);
        }
        if (hasBook) {
            totAmt += BOOK_SHIPPING_FEE; // 교재가 있으면 배송비 1회 부과
        }

        String ordNm = orderItems.get(0).getProdNm();
        if (orderItems.size() > 1) {
            ordNm += " 외 " + (orderItems.size() - 1) + "건";
        }

        // 결제 없이 방치된 이전 PENDING 주문을 만료시켜 회원당 PENDING을 1건으로 유지
        orderMapper.expirePendingOrders(userId);

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
                if (req.getItemQty() != null && req.getItemQty() != 1) {
                    throw new FinalProjectException(ErrorCode.BAD_REQUEST);
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

    @Override
    public PageResponse<OrderDto> getOrdersByUserId(String userId, int page, LocalDateTime from, LocalDateTime to) {

        PaginationInfo<OrderSearchCondition> paginationInfo = new PaginationInfo<>(10, page);
        OrderSearchCondition condition = new OrderSearchCondition();
        condition.setFrom(from);
        condition.setTo(to);
        paginationInfo.setDetailCondition(condition);

        List<OrderDto> orders = orderMapper.selectOrdersByUserId(userId, paginationInfo);
        int totalCount = orderMapper.selectOrderTotalCountByUserId(userId, paginationInfo);

        PageResponse<OrderDto> response = new PageResponse<>(orders, totalCount);
        return response;

    }

    @Override
    public OrderDto getOrderByOrderSn(Long ordSn, String userId) {
        OrderDto order = orderMapper.selectOrderByOrdSn(ordSn, userId);
        log.info("주문 상세 조회: ordSn={}, userId={}, order={}", ordSn, userId, order);
        if (order != null) {
            List<OrderItemDto> items = orderMapper.selectOrderItemsByOrderSn(order.getOrdSn(), userId);
            order.setItems(items);
        } else {
            throw new FinalProjectException(ErrorCode.ORDER_NOT_FOUND);
        }

        return order;
    }

}
