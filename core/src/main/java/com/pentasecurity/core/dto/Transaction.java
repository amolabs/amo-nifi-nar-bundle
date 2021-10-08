package com.pentasecurity.core.dto;

import com.google.gson.annotations.SerializedName;
import com.pentasecurity.core.service.RegisterTransactionCreator;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Transaction {
    private String type;
    private String sender;
    private String fee;
    @SerializedName("last_height")
    private String lastHeight;
    private Payload payload;
    private Signature signature;

    public Transaction() {

    }

    public Transaction(RegisterTransactionCreator.TxType type, String sender, String fee, String lastHeight) {
        this.type = type.name();
        this.sender = sender;
        this.fee = fee;
        this.lastHeight = lastHeight;
    }

    public Transaction(RegisterTransactionCreator.TxType type, String sender, String fee, String lastHeight, Payload payload) {
        this.type = type.name();
        this.sender = sender;
        this.fee = fee;
        this.lastHeight = lastHeight;
        this.payload = payload;
    }
}
