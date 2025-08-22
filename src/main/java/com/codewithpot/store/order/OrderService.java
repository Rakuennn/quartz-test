package com.codewithpot.store.order;

import com.codewithpot.store.common.constant.ResultDescriptionConstant;
import com.codewithpot.store.common.entity.shoply.OrderEntity;
import com.codewithpot.store.common.entity.shoply.UserEntity;
import com.codewithpot.store.common.repository.shoply.OrderRepository;
import com.codewithpot.store.common.repository.shoply.UserRepository;
import com.codewithpot.store.order.base.BaseOrderService;
import com.codewithpot.store.order.dto.request.CreateOrderRequest;
import com.codewithpot.store.order.dto.response.CreateOrderResponse;
import com.codewithpot.store.order.dto.response.GetOrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class OrderService implements BaseOrderService {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private UserRepository userRepo;

    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest req){
        UserEntity user = userRepo
                .findByUserId(req.getUserId())
                .orElseThrow();

        OrderEntity order = new OrderEntity();
        order.setUser(user).setStatus("PENDING");

        orderRepo.save(order);

        CreateOrderResponse res = new CreateOrderResponse();
        res.setStatus(ResultDescriptionConstant.DESCRIPTION_200000);
        res.setMessage("Create order successfully");
        return res;
    }

    @Override
    public List<GetOrderResponse> getOrders(){
        List<OrderEntity> orders = orderRepo.findAll();

        return orders.stream().map(order -> {
            GetOrderResponse res = new GetOrderResponse();
            res.setOrderId(order.getOrderId());
            res.setUserId(order.getUser().getUserId());
            res.setOrderStatus(order.getStatus());
            res.setStatus(ResultDescriptionConstant.DESCRIPTION_200000);
            res.setMessage("Get order successfully");
            return res;
        }).toList();
    }
}
