package com.pentasecurity.core.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class RegisterTxResponse {
    @SerializedName("result")
    private PostRegisterTxResult postRegisterTxResult;
}
