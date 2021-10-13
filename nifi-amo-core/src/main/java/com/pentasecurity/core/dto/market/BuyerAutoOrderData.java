package com.pentasecurity.core.dto.market;

import lombok.Getter;

import java.util.List;

@Getter
public class BuyerAutoOrderData {
    private long orderId;
    private long buyerId;
    private long sellerId;
    private long productId;
    private List<String> parcelIds;
}
