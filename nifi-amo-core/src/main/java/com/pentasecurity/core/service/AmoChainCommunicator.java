package com.pentasecurity.core.service;

import com.pentasecurity.core.dto.chain.GrantTxResponse;
import com.pentasecurity.core.dto.chain.RegisterTxResponse;
import com.pentasecurity.core.dto.chain.StatusResponse;
import com.pentasecurity.core.dto.chain.StorageResponse;
import com.pentasecurity.core.dto.rpc.*;
import com.pentasecurity.core.exception.RegisterTxException;
import com.pentasecurity.core.helper.RetrofitInitializer;
import com.pentasecurity.core.utils.CryptoUtils;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class AmoChainCommunicator {
    public static Retrofit retrofit = RetrofitInitializer.getRetrofitAmoChain();
    public static AmoChainHttpRequestor httpRequestor = retrofit.create(AmoChainHttpRequestor.class);

    public static String getLatestBlockHeight() {
        StatusResponse result = null;
        try {
            Response<StatusResponse> response = httpRequestor.postStatus(
                    new JsonRpc(null, "2.0", "status", "status")
            ).execute();

            result = response.body();
        } catch (IOException e) {
            log.error("request get latest block height error happened: {}", e.getMessage());
            throw new RuntimeException("request get latest block height error happened");
        }
        return result.getStatusResult().getSyncInfo().getLatestBlockHeight();
    }

    public static StorageResponse getStorage(String storageId) throws IOException {
        StorageResponse result;
        String storageIdHex = CryptoUtils.bytesToHex(storageId.getBytes(StandardCharsets.UTF_8));
        JsonRpc rpc = new JsonRpc(new ParamsStorage(storageIdHex), "2.0", "abci_query", "abci_query");
        Response<StorageResponse> response = httpRequestor.postStorage(rpc).execute();

        result = response.body();

        return result;
    }
    public static void requestRegisterTx(String signedTx) throws IOException {
        RegisterTxResponse result;
        JsonRpc rpc = new JsonRpc(new ParamsRegisterTx(signedTx), "2.0", "broadcast_tx_commit", "broadcast_tx_commit");
        Response<RegisterTxResponse> response = httpRequestor.postRegisterTx(rpc).execute();

        result = response.body();
        log.info("# TX hash: " + result.getRegisterTxResult().getHash());

        if (result.getRegisterTxResult().getCheckTx().getCode() != 0 ||
                result.getRegisterTxResult().getDeliverTx().getCode() != 0) {
            throw new RegisterTxException(
                    "CheckTx log: " + result.getRegisterTxResult().getCheckTx().getLog() +
                            ", DeliverTx log: " + result.getRegisterTxResult().getDeliverTx().getLog());
        }
    }

    public static boolean requestGrantTx(String signedTx) {
        // TODO implement
        GrantTxResponse result = null;
        try {
            JsonRpc rpc = new JsonRpc(new ParamsRegisterTx("aaaaa"),
                    "2.0", "broadcast_tx_commit", "broadcast_tx_commit");
            Response<GrantTxResponse> response = httpRequestor.postGrantTx(rpc).execute();
            result = response.body();
        } catch (IOException e) {
            log.error("request post register tx error happened: {}", e.getMessage());
            throw new RuntimeException("request post register tx error happened");
        }

        return result.getGrantTxResult().getCheckTx().getCode() == 0 &&
                result.getGrantTxResult().getDeliverTx().getCode() == 0;
    }
}
