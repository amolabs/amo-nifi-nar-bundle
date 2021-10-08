package com.pentasecurity.core.dto;

import com.google.gson.annotations.SerializedName;

public class Metadata {
    @SerializedName("owner")
    private String owner;

    @SerializedName("hash")
    private String hash;

    public Metadata(String owner, String hash) {
        this.owner = owner;
        this.hash = hash;
    }
}
