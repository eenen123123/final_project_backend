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
import kr.or.ddit.finalProject.dto.coupon.CouponDto;
import kr.or.ddit.finalProject.dto.coupon.MemberCouponPointDto;
import kr.or.ddit.finalProject.dto.order.MemberAddressDto;
import kr.or.ddit.finalProject.dto.order.OrderCreateRequest;
import kr.or.ddit.finalProject.dto.order.OrderShippingDto;
import kr.or.ddit.finalProject.dto.order.OrderStatus;
import kr.or.ddit.finalProject.mapper.coupon.CouponMapper;
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
    private final CouponMapper couponMapper;

    @Override
    @Transactional
    public OrderDto createOrder(String userId, List<OrderItemDto> items, long pointAmt, AssetType pointType,
                                OrderShippingDto shipping, boolean saveToAddressBook,
                                List<OrderCreateRequest.CouponApplication> coupons) {
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

        // 쿠폰 할인 적용 (배송비 추가 전 상품금액에만 적용)
        // availableCoupons는 예약 단계에서도 재사용 (중복 조회 방지)
        List<MemberCouponPointDto> availableCoupons = (coupons != null && !coupons.isEmpty())
                ? couponMapper.selectAvailableCouponsForCheckout(userId)
                : java.util.Collections.emptyList();

        if (!availableCoupons.isEmpty()) {
            for (OrderCreateRequest.CouponApplication ca : coupons) {
                MemberCouponPointDto uc = availableCoupons.stream()
                        .filter(c -> c.getMcpntSn().equals(ca.getMcpntSn()))
                        .findFirst().orElse(null);
                if (uc == null || uc.getDiscType() == null) continue;

                // prodSn으로 쿠폰이 적용된 특정 상품만 baseAmt로 사용 (전체 동일 타입 합산 방지)
                long baseAmt = (ca.getProdSn() != null && ca.getProdDivCd() != null)
                        ? orderItems.stream()
                                .filter(i -> ca.getProdSn().equals(i.getProdSn())
                                        && i.getProdDivCd().name().equals(ca.getProdDivCd()))
                                .mapToLong(i -> i.getProdPrice() * i.getItemQty())
                                .findFirst().orElse(0L)
                        : orderItems.stream()
                                .filter(i -> "ALL".equals(uc.getUseLimitCd()) || i.getProdDivCd().name().equals(uc.getUseLimitCd()))
                                .mapToLong(i -> i.getProdPrice() * i.getItemQty())
                                .sum();

                long disc = uc.getDiscType().name().equals("FIXED")
                        ? Math.min(uc.getDiscAmt() != null ? uc.getDiscAmt() : 0, baseAmt)
                        : (long) Math.floor(baseAmt * (uc.getDiscRate() != null ? uc.getDiscRate() : 0) / 100.0);

                totAmt = Math.max(0, totAmt - disc);
                log.info("쿠폰 할인 적용 - mcpntSn: {}, disc: {}", uc.getMcpntSn(), disc);
            }
        }
        // 쿠폰 예약은 주문 INSERT 후 처리 (ordSn 필요)

        // 배송비 추가 (쿠폰 할인 후)
        if (hasBook) {
            totAmt += BOOK_SHIPPING_FEE;
        }

        // 포인트 사용 검증 (토스 API 호출 전 선검증, 실제 차감은 결제 승인 후 수행)
        if (pointAmt > 0) {
            if (pointType == null || pointType == AssetType.COUPON) {
                throw new FinalProjectException(ErrorCode.POINT_INVALID_TYPE);
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

        // 0원 결제 방어 (쿠폰+포인트 전액 할인 시 Toss가 0원 결제를 허용하지 않음)
        if (totAmt <= 0) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
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

        // 쿠폰 예약 (ordSn 확보 후) — availableCoupons 재사용, 예약 실패 시 트랜잭션 롤백
        for (OrderCreateRequest.CouponApplication ca : coupons != null ? coupons : java.util.Collections.<OrderCreateRequest.CouponApplication>emptyList()) {
            boolean valid = availableCoupons.stream().anyMatch(c -> c.getMcpntSn().equals(ca.getMcpntSn()));
            if (!valid) continue;
            int reserved = couponMapper.reserveCoupon(ca.getMcpntSn(), order.getOrdSn());
            if (reserved == 0) {
                // 할인 계산 후 쿠폰이 이미 사용됐거나 만료된 경우 → 주문 전체 롤백
                log.warn("쿠폰 예약 실패 (이미 사용 or 만료) - mcpntSn: {}, userId: {}", ca.getMcpntSn(), userId);
                throw new FinalProjectException(ErrorCode.COUPON_INACTIVE);
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
    public int getTotalOrderCount() {
        return orderMapper.countTotalOrders();
    }

    @Override
    public PageResponse<OrderDto> getAllOrders(PaginationInfo<OrderSearchCondition> paginationInfo) {
        List<OrderDto> list = orderMapper.selectAllOrders(paginationInfo);
        int totalCount = orderMapper.countAllOrders(paginationInfo);
        return new PageResponse<>(list, totalCount);
    }

    @Override
    public List<OrderDto> getCancelRequestList() {
        return orderMapper.selectCancelRequestList();
    }

    @Override
    public OrderDto getCancelDetailByOrdSn(Long ordSn) {
        OrderDto order = orderMapper.selectOrderByOrdSnForAdmin(ordSn);
        if (order == null) {
            throw new FinalProjectException(ErrorCode.ORDER_NOT_FOUND);
        }
        List<OrderItemDto> items = orderMapper.selectOrderItemsByOrdSn(order.getOrdSn());
        order.setItems(items);
        return order;
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
