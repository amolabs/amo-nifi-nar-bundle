package com.pentasecurity.core.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Signature {
    private String pubkey;
    @SerializedName("sig_bytes")
    private String sigBytes;

    public Signature() {
    }

    public Signature(String pubkey, String sigBytes) {
        this.pubkey = pubkey;
        this.sigBytes = sigBytes;
    }
}
