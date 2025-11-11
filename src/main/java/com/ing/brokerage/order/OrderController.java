package com.ing.brokerage.order;

import com.ing.brokerage.exception.BusinessException;
import com.ing.brokerage.exception.RecordNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize(value = "@userSecurity.isSelfOrAdmin(authentication, #request.customerId)")
    ResponseEntity<OrderResponse> createOrder(@RequestBody OrderCreateRequest request) throws RecordNotFoundException, BusinessException {

        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.created(ServletUriComponentsBuilder
                                          .fromCurrentRequest().path("/{orderId}")
                                          .buildAndExpand(response.getId()).toUri())
                             .body(response);
    }

    @PatchMapping("/{orderId}")
    @PreAuthorize(value = "hasRole('ADMIN')")
    ResponseEntity<OrderResponse> matchOrder(@PathVariable("orderId") Long orderId) throws RecordNotFoundException, BusinessException {

        return ResponseEntity.ok(orderService.matchOrder(orderId));
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize(value = "hasRole('ADMIN')")
    ResponseEntity<OrderResponse> cancelOrder(@PathVariable("orderId") Long orderId) throws RecordNotFoundException, BusinessException {

        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }

    @GetMapping
    @PreAuthorize(value = "@userSecurity.isSelfOrAdmin(authentication, #searchRequest.customerId)")
    ResponseEntity<Page<OrderResponse>> listOrders(
        @ParameterObject OrderSearchRequest searchRequest,
        @ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(orderService.listOrders(searchRequest, pageable));
    }

}
