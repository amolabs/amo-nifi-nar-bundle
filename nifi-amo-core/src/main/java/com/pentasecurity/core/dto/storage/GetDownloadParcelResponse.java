package com.pentasecurity.core.dto.storage;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class GetDownloadParcelResponse {
    @SerializedName("id")
    private String parcelId;
    private String data;
}
