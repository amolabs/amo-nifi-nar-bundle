package com.pentasecurity.core.dto;

import com.google.gson.annotations.SerializedName;

public class Operation {
    @SerializedName("name")
    private String name;

    @SerializedName("hash")
    private String hash;

    public Operation(String name, String hash) {
        this.name = name;
        this.hash = hash;
    }
}
