package com.pentasecurity.core.dto.chain;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class StorageInfo {
    private String owner;
    private String url;

    @SerializedName("registration_fee")
    private String registrationFee;

    @SerializedName("hosting_fee")
    private String hostingFee;

    private boolean active;
}
