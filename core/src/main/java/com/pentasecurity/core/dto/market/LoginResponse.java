package com.pentasecurity.core.dto.market;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class LoginResponse {
    @SerializedName("data")
    private LoginData data;
}
