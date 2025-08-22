package com.codewithpot.store.order.base;

import com.codewithpot.store.order.dto.request.CreateOrderRequest;
import com.codewithpot.store.order.dto.response.CreateOrderResponse;
import com.codewithpot.store.order.dto.response.GetOrderResponse;

import java.util.List;

public interface BaseOrderService   {
    CreateOrderResponse createOrder(CreateOrderRequest req);
    List<GetOrderResponse> getOrders();
}
