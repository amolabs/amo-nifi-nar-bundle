package com.pentasecurity.core.dto.rpc;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ParamsRegisterTx {
    @SerializedName("tx")
    private Tx tx;
}
