package kr.or.ddit.controller.order;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.order.OrderDto;
import kr.or.ddit.finalProject.dto.order.OrderSearchCondition;
import kr.or.ddit.finalProject.dto.order.OrderShippingDto;
import kr.or.ddit.finalProject.dto.order.ShippingStatus;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.service.order.OrderService;
import kr.or.ddit.finalProject.service.order.OrderShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
    private final OrderShippingService orderShippingService;

    @GetMapping
    public String orderList(
            @RequestParam(defaultValue = "all") String view,
            @RequestParam(defaultValue = "1") int page,
            // 전체 주문 검색
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String ordStatCd,
            // 배송 관리 검색
            @RequestParam(required = false) String shippingKeyword,
            @RequestParam(required = false) ShippingStatus dlvryStatCd,
            @RequestParam(defaultValue = "desc") String sort,
            Model model) {

        // 공통 카운트 (항상 조회)
        model.addAttribute("orderCount", orderService.getTotalOrderCount());
        model.addAttribute("cancelCount", orderService.getCancelRequestList().size());

        Map<String, Object> statusSummary = orderShippingService.getShippingStatusSummary();
        long shippingCount = ((Number) statusSummary.getOrDefault("READY_COUNT", 0)).longValue()
                           + ((Number) statusSummary.getOrDefault("SHIPPING_COUNT", 0)).longValue()
                           + ((Number) statusSummary.getOrDefault("DELIVERED_COUNT", 0)).longValue();
        model.addAttribute("shippingCount", shippingCount);

        String orderDir = "asc".equals(sort) ? "ASC" : "DESC";
        model.addAttribute("view", view);
        model.addAttribute("sort", sort);

        if ("shipping".equals(view)) {
            // 배송 관리 뷰
            PaginationInfo<OrderShippingDto> paginationInfo = new PaginationInfo<>(10, 5, page, null, orderDir);
            OrderShippingDto condition = new OrderShippingDto();
            condition.setKeyword(shippingKeyword);
            condition.setDlvryStatCd(dlvryStatCd);
            paginationInfo.setDetailCondition(condition);

            PageResponse<OrderShippingDto> result = orderShippingService.getShippingList(paginationInfo);
            model.addAttribute("shippingList", result.getItems());
            model.addAttribute("paginationInfo", paginationInfo);
            model.addAttribute("statusSummary", statusSummary);

        } else if ("cancel".equals(view)) {
            // 환불/취소 관리 뷰
            model.addAttribute("cancelList", orderService.getCancelRequestList());

        } else {
            // 전체 주문 뷰 (기본)
            PaginationInfo<OrderSearchCondition> paginationInfo = new PaginationInfo<>(10, 5, page, null, orderDir);
            OrderSearchCondition condition = new OrderSearchCondition();
            condition.setUserId(userId);
            condition.setKeyword(keyword);
            condition.setOrdStatCd(ordStatCd);
            paginationInfo.setDetailCondition(condition);

            PageResponse<OrderDto> result = orderService.getAllOrders(paginationInfo);
            model.addAttribute("orderList", result.getItems());
            model.addAttribute("paginationInfo", paginationInfo);
        }

        return "admin:/order/order_list";
    }
}
