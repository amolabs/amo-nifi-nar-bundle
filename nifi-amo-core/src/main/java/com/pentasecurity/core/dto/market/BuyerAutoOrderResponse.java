package com.pentasecurity.core.dto.market;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class BuyerAutoOrderResponse {
    @SerializedName("data")
    private List<BuyerAutoOrderData> data = new ArrayList<>();
}
