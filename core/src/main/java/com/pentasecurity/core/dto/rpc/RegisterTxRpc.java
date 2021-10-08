package com.pentasecurity.core.dto.rpc;


import com.google.gson.annotations.SerializedName;

public class RegisterTxRpc extends JsonRpc {
    @SerializedName("params")
    private ParamsRegisterTx params;
    public RegisterTxRpc(ParamsRegisterTx params, String jsonrpc, String id, String method) {
        super(jsonrpc, id, method);
        this.params = params;
    }
}
