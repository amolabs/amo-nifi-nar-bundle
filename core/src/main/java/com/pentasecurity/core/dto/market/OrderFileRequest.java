package com.pentasecurity.core.dto.market;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OrderFileRequest {
    private long buyerId;
    private long sellerId;
    private long productId;
    private long fileId;
    private String orderType;
}
