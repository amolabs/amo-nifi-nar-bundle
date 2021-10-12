package com.pentasecurity.core.dto.chain;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class GrantTxResponse {
    @SerializedName("result")
    private PostGrantTxResult postGrantTxResult;
}
