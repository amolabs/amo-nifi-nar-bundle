package com.pentasecurity.core.service;

import com.pentasecurity.core.dto.PostAuthRequest;
import com.pentasecurity.core.dto.PostAuthResponse;
import com.pentasecurity.core.dto.PostParcelsResponse;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface AmoStorageHttpRequestor {
    @POST("auth")
    Call<PostAuthResponse> postAuth(@Body PostAuthRequest body);

    @Multipart
    @POST("parcels")
    Call<PostParcelsResponse> postParcels(@HeaderMap Map<String, String> headers,
                                          @PartMap Map<String, RequestBody> params,
                                          @Part MultipartBody.Part file);
}
