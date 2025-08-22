package com.codewithpot.store.order.dto.response;

import com.codewithpot.store.common.base.BaseResponse;
import com.codewithpot.store.common.entity.shoply.UserEntity;
import lombok.Data;

import java.util.UUID;
@Data
public class GetOrderResponse extends BaseResponse {
    private UUID orderId;
    private UUID userId;
    private String orderStatus;
}
