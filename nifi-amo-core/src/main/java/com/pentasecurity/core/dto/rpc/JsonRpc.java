package com.pentasecurity.core.dto.rpc;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class JsonRpc<T> {
    private T params;
    private String jsonrpc;
    private String id;
    private String method;
}
