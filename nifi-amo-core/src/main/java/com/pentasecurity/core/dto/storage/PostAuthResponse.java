package com.pentasecurity.core.dto.storage;

import com.google.gson.annotations.SerializedName;

public class PostAuthResponse {
    @SerializedName("token")
    private String token;

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return "PostAuthResponse{" + "token=" + token + "}";
    }
}
