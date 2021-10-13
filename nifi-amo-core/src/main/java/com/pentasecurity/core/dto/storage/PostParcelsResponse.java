package com.pentasecurity.core.dto.storage;

import com.google.gson.annotations.SerializedName;

public class PostParcelsResponse {
    @SerializedName("id")
    private String parcelId;

    public String getParcelId() {
        return parcelId;
    }

    @Override
    public String toString() {
        return "PostParcelsResponse{" + "parcelId=" + parcelId + "}";
    }
}
