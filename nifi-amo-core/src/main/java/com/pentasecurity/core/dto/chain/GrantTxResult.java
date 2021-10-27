package com.pentasecurity.core.dto.chain;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class GrantTxResult {
    @SerializedName("check_tx")
    private CheckTx checkTx;

    @SerializedName("deliver_tx")
    private DeliverTx deliverTx;
}
