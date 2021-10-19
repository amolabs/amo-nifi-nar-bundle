package com.pentasecurity.core.dto.rpc;


import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class RegisterTxRpc<T> extends JsonRpc {

    public RegisterTxRpc(T params, String jsonrpc, String id, String method) {
        super(params, jsonrpc, id, method);
    }
}
