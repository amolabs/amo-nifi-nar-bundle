package com.pentasecurity.core.dto.market;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PatchOrderRequest {
    private long orderId;
    private String orderStatus;
}
