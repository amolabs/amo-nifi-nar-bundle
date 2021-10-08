package com.pentasecurity.core.service;

import com.pentasecurity.core.dto.RegisterTxResponse;
import com.pentasecurity.core.dto.StatusResponse;
import com.pentasecurity.core.dto.rpc.JsonRpc;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AmoChainHttpRequestor {
    @POST("/")
    Call<StatusResponse> postStatus(@Body JsonRpc jsonRpc);

    @POST("/")
    Call<RegisterTxResponse> postRegisterTx(@Body JsonRpc jsonRpc);
}
