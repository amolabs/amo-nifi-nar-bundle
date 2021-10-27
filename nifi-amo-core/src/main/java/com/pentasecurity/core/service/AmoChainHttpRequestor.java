package com.pentasecurity.core.service;

import com.pentasecurity.core.dto.chain.GrantTxResponse;
import com.pentasecurity.core.dto.chain.RegisterTxResponse;
import com.pentasecurity.core.dto.chain.StatusResponse;
import com.pentasecurity.core.dto.chain.StorageResponse;
import com.pentasecurity.core.dto.rpc.JsonRpc;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AmoChainHttpRequestor {
    @POST("/")
    Call<StatusResponse> postStatus(@Body JsonRpc jsonRpc);

    @POST("/")
    Call<StorageResponse> postStorage(@Body JsonRpc jsonRpc);

    @POST("/")
    Call<RegisterTxResponse> postRegisterTx(@Body JsonRpc jsonRpc);

    @POST("/")
    Call<GrantTxResponse> postGrantTx(@Body JsonRpc jsonRpc);
}
