package com.pentasecurity.core.dto.chain;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class PostStatusResult {
    @SerializedName("sync_info")
    private SyncInfo syncInfo;
}
