package kr.or.ddit.finalProject.service.order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.cart.ProductType;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.coupon.AssetType;
import kr.or.ddit.finalProject.dto.order.OrderDto;
import kr.or.ddit.finalProject.dto.order.OrderItemDto;
import kr.or.ddit.finalProject.dto.order.OrderSearchCondition;
import kr.or.ddit.finalProject.dto.order.MemberAddressDto;
import kr.or.ddit.finalProject.dto.order.OrderShippingDto;
import kr.or.ddit.finalProject.dto.order.OrderStatus;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.order.OrderMapper;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.service.coupon.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final long BOOK_SHIPPING_FEE = 3000; // 교재 포함 시 배송비

    private final OrderMapper orderMapper;
    private final PointService pointService;
    private final OrderShippingService orderShippingService;
    private final MemberAddressService memberAddressService;

    @Override
    @Transactional
    public OrderDto createOrder(String userId, List<OrderItemDto> items, long pointAmt, AssetType pointType, OrderShippingDto shipping, boolean saveToAddressBook) {
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
            // 주문 INSERT 전에 배송지 선검증 (INSERT 후 검증 시 고아 PENDING 주문 발생 방지)
            if (shipping == null) {
                throw new FinalProjectException(ErrorCode.BAD_REQUEST);
            }
            if (shipping.getBuyerNm() == null || shipping.getBuyerNm().isBlank()) {
                throw new FinalProjectException(ErrorCode.SHIPPING_BUYER_NAME_REQUIRED);
            }
            if (shipping.getBuyerTel() == null || shipping.getBuyerTel().isBlank()) {
                throw new FinalProjectException(ErrorCode.SHIPPING_BUYER_TEL_REQUIRED);
            }
        }

        // 포인트 사용 검증 (토스 API 호출 전 선검증, 실제 차감은 결제 승인 후 수행)
        if (pointAmt > 0) {
            if (pointType == null || pointType == AssetType.COUPON) {
                throw new FinalProjectException(ErrorCode.POINT_INVALID_TYPE);
            }
            if (pointAmt < 1_000) {
                throw new FinalProjectException(ErrorCode.POINT_MINIMUM_USAGE);
            }
            if (pointAmt > totAmt) {
                throw new FinalProjectException(ErrorCode.BAD_REQUEST);
            }
            long balance = pointService.getPointBalance(userId, pointType);
            if (balance < pointAmt) {
                throw new FinalProjectException(ErrorCode.POINT_INSUFFICIENT_BALANCE);
            }
            // 포인트 차감 후 실제 현금 결제액 (토스 결제 금액 = 상품금액 - 포인트)
            totAmt -= pointAmt;
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
                .pointAmt(pointAmt > 0 ? Long.valueOf(pointAmt) : null)
                .pointType(pointAmt > 0 ? pointType : null)
                .build();
        orderMapper.insertOrder(order);

        for (OrderItemDto item : orderItems) {
            item.setOrdSn(order.getOrdSn());
            orderMapper.insertOrderItem(item);
        }

        if (hasBook) {
            shipping.setOrdSn(order.getOrdSn());
            shipping.setRgtrId(userId);
            orderShippingService.registerOrderShipping(shipping);

            if (saveToAddressBook) {
                MemberAddressDto addressDto = MemberAddressDto.builder()
                        .userId(userId)
                        .addressNm("기본 배송지")
                        .receiverNm(shipping.getReceiverNm())
                        .receiverTel(shipping.getReceiverTel())
                        .zipCd(shipping.getZipCd())
                        .addrRoad(shipping.getAddrRoad())
                        .addrJibun(shipping.getAddrJibun())
                        .addrDtl(shipping.getAddrDtl())
                        .deliveryMsg(shipping.getDeliveryMsg())
                        .defaultYn("N")
                        .build();
                memberAddressService.registerAddress(addressDto);
                log.info("주소록 저장 - userId: {}", userId);
            }
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

        return new PageResponse<>(orders, totalCount);
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
