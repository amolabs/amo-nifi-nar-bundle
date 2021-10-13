package com.pentasecurity.core.dto.market;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.List;

@Getter
public class BuyerAutoOrderResponse {
    @SerializedName("data")
    private List<BuyerAutoOrderData> data;
}
