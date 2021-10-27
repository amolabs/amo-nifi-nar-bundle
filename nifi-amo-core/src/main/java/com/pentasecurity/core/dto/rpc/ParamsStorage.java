package com.pentasecurity.core.dto.rpc;

import lombok.AllArgsConstructor;


public class ParamsStorage {
    private String path = "/storage";
    private String data;

    public ParamsStorage(String data) {
        this.data = data;
    }
}
