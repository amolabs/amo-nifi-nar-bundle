package com.pentasecurity.core.dto.rpc;

public class StatusRpc extends JsonRpc {
    public StatusRpc(String jsonrpc, String id, String method) {
        super(jsonrpc, id, method);
    }
}
