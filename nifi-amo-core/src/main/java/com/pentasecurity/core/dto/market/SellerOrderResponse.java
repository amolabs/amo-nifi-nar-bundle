package com.pentasecurity.core.dto.market;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SellerOrderResponse {
    @SerializedName("data")
    private List<SellerOrderData> data = new ArrayList<>();
}
