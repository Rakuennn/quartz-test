package com.codewithpot.store.common.base;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public abstract class BaseResponse {
    private String status;
    private String message;
}
