package com.pentasecurity.core.dto.market;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.List;

@Getter
public class SellerAutoOrderResponse {
    @SerializedName("data")
    private List<SellerAutoOrderData> data;
}
