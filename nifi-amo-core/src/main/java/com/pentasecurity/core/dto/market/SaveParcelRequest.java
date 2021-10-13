package com.pentasecurity.core.dto.market;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SaveParcelRequest {
    private String parcelId;
    private long productId;
    private String price;
}
