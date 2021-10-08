package com.pentasecurity.core.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class SyncInfo {
    @SerializedName("latest_block_height")
    private String latestBlockHeight;
}
