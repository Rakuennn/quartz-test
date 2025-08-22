package com.codewithpot.store.order;

import com.codewithpot.store.order.base.BaseOrderController;
import com.codewithpot.store.order.dto.request.CreateOrderRequest;
import com.codewithpot.store.order.dto.response.CreateOrderResponse;
import com.codewithpot.store.order.dto.response.GetOrderResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Order")
public class OrderController extends BaseOrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("orders/create")
    @Override
    public ResponseEntity<CreateOrderResponse> createOrder(
            @RequestBody CreateOrderRequest req
    ){
        return ResponseEntity.ok(orderService.createOrder(req));
    }

    @GetMapping("orders/get")
    @Override
    public  ResponseEntity<List<GetOrderResponse>> getOrders(){
        return ResponseEntity.ok(orderService.getOrders());
    }
}
