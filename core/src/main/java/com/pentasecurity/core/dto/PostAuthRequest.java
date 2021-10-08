package com.pentasecurity.core.dto;

import com.google.gson.annotations.SerializedName;

public class PostAuthRequest {
    @SerializedName("user")
    private String user;

    @SerializedName("operation")
    private Operation operation;

    public PostAuthRequest(String user, Operation operation) {
        this.user = user;
        this.operation = operation;
    }
}
