package com.pentasecurity.core.dto.market;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class ParcelResponse {
    private String message;
    private int status;
    private ParcelData data;
}
