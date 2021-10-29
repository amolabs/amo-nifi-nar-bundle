package com.pentasecurity.core.dto.storage;

import com.google.gson.annotations.SerializedName;
import lombok.Setter;

@Setter
public class Operation {
    private String name;
    private String hash;
    private String id;

    public Operation(String name) {
        this.name = name;
    }
}
