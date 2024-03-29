package com.pentasecurity.core.dto.market;

import lombok.Getter;

import java.util.List;

@Getter
public class SellerAutoOrderData {
    private long autoOrderId;
    private long buyerId;
    private long sellerId;
    private long productId;
    private String recipient;
    private List<String> parcelIds;
}
