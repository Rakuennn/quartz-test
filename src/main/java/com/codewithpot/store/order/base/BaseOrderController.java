package com.codewithpot.store.order.base;

import com.codewithpot.store.common.base.BaseController;
import com.codewithpot.store.order.dto.request.CreateOrderRequest;
import com.codewithpot.store.order.dto.response.CreateOrderResponse;
import com.codewithpot.store.order.dto.response.GetOrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

import java.util.List;

public abstract class BaseOrderController extends BaseController {

    @Operation(summary = "Create order")
    public abstract ResponseEntity<CreateOrderResponse> createOrder(CreateOrderRequest req);

    @Operation(summary = "Get orders")
    public abstract ResponseEntity<List<GetOrderResponse>> getOrders();
}
