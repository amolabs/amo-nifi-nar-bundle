package com.pentasecurity.core.dto.rpc;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JsonRpc {
    public JsonRpc(){}
    @SerializedName("jsonrpc")
    private String jsonrpc;

    @SerializedName("id")
    private String id;

    @SerializedName("method")
    private String method;
}
