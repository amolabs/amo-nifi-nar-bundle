package com.pentasecurity.core.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class PostRegisterTxResult {
    @SerializedName("check_tx")
    private CheckTx checkTx;

    @SerializedName("deliver_tx")
    private DeliverTx deliverTx;
}
