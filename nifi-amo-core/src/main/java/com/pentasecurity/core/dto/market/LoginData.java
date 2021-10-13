package com.pentasecurity.core.dto.market;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class LoginData {
    @SerializedName("accessToken")
    private String accessToken;
}
