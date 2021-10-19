package com.pentasecurity.core.service;

import com.google.gson.Gson;
import com.pentasecurity.core.dto.chain.Payload;
import com.pentasecurity.core.dto.chain.Transaction;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.Map;

@Slf4j
public abstract class TransactionCreator {
    protected abstract Transaction createSignedTx();

    protected abstract Transaction createUnsignedTx();

    protected Transaction sign() {
        return null;
    }

    protected Transaction serializeUnsignedTx() {
        return null;
    }

    protected Transaction serializeSignedTx() {
        return null;
    }

    protected Transaction createTx(Transaction.TxType type, String sender, BigInteger fee, int last_height, Map<String, Object> map) {
        Gson gson = new Gson();
        Transaction amoTx = new Transaction(type, sender, fee.toString(), String.valueOf(last_height));
        Payload payload = new Payload();
        switch (type) {
            case register:
                payload.setTarget((String) map.get("parcelId"));

                if (map.containsKey("custody")) {
                    payload.setCustody((String) map.get("custody"));
                }

                if (map.containsKey("proxy_account")) {
                    payload.setProxyAccount((String) map.get("proxy_account"));
                }

                if (map.containsKey("extraInfo")) {
                    String extraJson = (String) map.get("extraInfo");
                    Map extra = gson.fromJson(extraJson, Map.class);
                    payload.setExtra(extra);
                }
                break;
            case request:
                // TODO implement
                break;
            case grant:
                // TODO implement
                payload.setTarget((String) map.get("parcelId"));
                payload.setCustody((String) map.get("custody"));
                payload.setRecipient((String) map.get("recipient"));

                break;
            case stake:
                payload.setValidator((String) map.get("validator"));
                payload.setAmount((String) map.get("amount"));
                break;
            case transfer:
                payload.setTo((String) map.get("to"));
                payload.setAmount((String) map.get("amount"));
                break;
            case setup:
                payload.setStorage((Integer) map.get("storageID")); // storage 값은 쌍따옴표를 붙이면 안됨
                payload.setUrl((String) map.get("url"));
                payload.setRegistrationFee((String) map.get("registrationFee"));
                payload.setHostingFee((String) map.get("hostingFee"));
                break;
            case close:
                payload.setStorage((Integer) map.get("storageID"));
                break;
            default:
                break;
        }
        amoTx.setPayload(payload);
        log.info("#### unsigned tx : " + gson.toJson(amoTx));
        return amoTx;
    }
}
