package com.codewithpot.store.order.dto.request;

import com.codewithpot.store.common.base.BaseRequest;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class CreateOrderRequest extends BaseRequest {
    private UUID userId;
}
