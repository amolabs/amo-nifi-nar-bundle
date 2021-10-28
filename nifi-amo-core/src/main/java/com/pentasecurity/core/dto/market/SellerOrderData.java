package com.pentasecurity.core.dto.market;

import lombok.Getter;

import java.util.List;

@Getter
public class SellerOrderData {
    private long orderId;
    private long buyerId;
    private long sellerId;
    private long productId;
    private String recipient;
    private List<String> parcelIds;
}
