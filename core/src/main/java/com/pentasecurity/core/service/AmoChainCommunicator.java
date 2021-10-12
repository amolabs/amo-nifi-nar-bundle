package com.pentasecurity.core.service;

import com.pentasecurity.core.dto.chain.GrantTxResponse;
import com.pentasecurity.core.dto.chain.RegisterTxResponse;
import com.pentasecurity.core.dto.chain.StatusResponse;
import com.pentasecurity.core.dto.rpc.*;
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

    public static boolean requestRegisterTx(String signedTx) {
        RegisterTxResponse result = null;
        try {
            Response<RegisterTxResponse> response = httpRequestor.postRegisterTx(
                    new RegisterTxRpc(new ParamsRegisterTx(new Tx(signedTx)), "2.0", "broadcast_tx_commit", "broadcast_tx_commit")
            ).execute();
            result = response.body();
        } catch (IOException e) {
            log.error("request post register tx error happened: {}", e.getMessage());
            throw new RuntimeException("request post register tx error happened");
        }

        return result.getPostRegisterTxResult().getCheckTx().getCode() == 0 &&
                result.getPostRegisterTxResult().getDeliverTx().getCode() == 0;
    }

    public static boolean requestGrantTx(String signedTx) {
        GrantTxResponse result = null;
        try {
            Response<GrantTxResponse> response = httpRequestor.postGrantTx(
                    new RegisterTxRpc(new ParamsRegisterTx(new Tx(signedTx)), "2.0", "broadcast_tx_commit", "broadcast_tx_commit")
            ).execute();
            result = response.body();
        } catch (IOException e) {
            log.error("request post register tx error happened: {}", e.getMessage());
            throw new RuntimeException("request post register tx error happened");
        }

        return result.getPostGrantTxResult().getCheckTx().getCode() == 0 &&
                result.getPostGrantTxResult().getDeliverTx().getCode() == 0;
    }
}
