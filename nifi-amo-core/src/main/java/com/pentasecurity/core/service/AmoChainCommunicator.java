package com.pentasecurity.core.service;

import com.google.gson.Gson;
import com.pentasecurity.core.dto.chain.GrantTxResponse;
import com.pentasecurity.core.dto.chain.RegisterTxResponse;
import com.pentasecurity.core.dto.chain.StatusResponse;
import com.pentasecurity.core.dto.rpc.*;
import com.pentasecurity.core.exception.RegisterTxException;
import com.pentasecurity.core.helper.RetrofitInitializer;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;

@Slf4j
public class AmoChainCommunicator {
    public static Retrofit retrofit = RetrofitInitializer.getRetrofitAmoChain();
    public static AmoChainHttpRequestor httpRequestor = retrofit.create(AmoChainHttpRequestor.class);

    public static String getLatestBlockHeight() {
        StatusResponse result = null;
        try {
            Response<StatusResponse> response = httpRequestor.postStatus(
                    new StatusRpc("2.0", "status", "status")
            ).execute();

            result = response.body();
        } catch (IOException e) {
            log.error("request get latest block height error happened: {}", e.getMessage());
            throw new RuntimeException("request get latest block height error happened");
        }
        return result.getPostStatusResult().getSyncInfo().getLatestBlockHeight();
    }

    public static void requestRegisterTx(String signedTx) throws IOException {
        RegisterTxResponse result;
        RegisterTxRpc registerTxRpc = new RegisterTxRpc(new ParamsRegisterTx(signedTx), "2.0", "broadcast_tx_commit", "broadcast_tx_commit");
        Response<RegisterTxResponse> response = httpRequestor.postRegisterTx(registerTxRpc).execute();

        result = response.body();
        log.info("# TX hash: " + result.getPostRegisterTxResult().getHash());

        if (result.getPostRegisterTxResult().getCheckTx().getCode() != 0 ||
                result.getPostRegisterTxResult().getDeliverTx().getCode() != 0) {
            throw new RegisterTxException(
                    "CheckTx log: " + result.getPostRegisterTxResult().getCheckTx().getLog() +
                            ", DeliverTx log: " + result.getPostRegisterTxResult().getDeliverTx().getLog());
        }
    }

    public static boolean requestGrantTx(String signedTx) {
        GrantTxResponse result = null;
        try {
            RegisterTxRpc registerTxRpc = new RegisterTxRpc<>(new ParamsRegisterTx("aaaaa"),
                    "2.0", "broadcast_tx_commit", "broadcast_tx_commit");
            Response<GrantTxResponse> response = httpRequestor.postGrantTx(registerTxRpc).execute();
            result = response.body();
        } catch (IOException e) {
            log.error("request post register tx error happened: {}", e.getMessage());
            throw new RuntimeException("request post register tx error happened");
        }

        return result.getPostGrantTxResult().getCheckTx().getCode() == 0 &&
                result.getPostGrantTxResult().getDeliverTx().getCode() == 0;
    }
}
