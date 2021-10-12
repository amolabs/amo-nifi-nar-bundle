package com.pentasecurity.core.dto.chain;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class Payload {
    // register
    private String target;
    private String custody;
    @SerializedName("proxy_account")
    private String proxyAccount;
    private Map extra;

    // grant
    private String recipient;

    // stake
    private String validator;
    private String amount; // stake or transfer

    // transfer
    private String to;

    // setup
    private Integer storage; // setup or close
    private String url;
    @SerializedName("registration_fee")
    private String registrationFee;
    @SerializedName("hosting_fee")
    private String hostingFee;

    public Payload() {
    }

    // register
    public Payload(String target, String custody, String proxyAccount) {
        this.target = target;
        this.custody = custody;
        this.proxyAccount = proxyAccount;
    }

    // register with extra
    public Payload(String target, String custody, String proxyAccount, Map extra) {
        this.target = target;
        this.custody = custody;
        this.proxyAccount = proxyAccount;
        this.extra = extra;
    }

    // transfer
    public Payload(String to, String amount) {
        this.to = to;
        this.amount = amount;
    }

    // setup
    public Payload(int storage, String url, String registrationFee, String hostingFee) {
        this.storage = storage;
        this.url = url;
        this.registrationFee = registrationFee;
        this.hostingFee = hostingFee;
    }

    // close
    public Payload(int storage) {
        this.storage = storage;
    }
}
