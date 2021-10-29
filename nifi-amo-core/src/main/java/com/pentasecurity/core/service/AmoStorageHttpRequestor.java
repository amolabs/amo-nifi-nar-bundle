package com.pentasecurity.core.service;

import com.pentasecurity.core.dto.storage.GetDownloadParcelResponse;
import com.pentasecurity.core.dto.storage.PostAuthRequest;
import com.pentasecurity.core.dto.storage.PostAuthResponse;
import com.pentasecurity.core.dto.storage.PostParcelsResponse;
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
    Call<PostParcelsResponse> postParcelsByMultipart(@HeaderMap Map<String, String> headers,
                                          @PartMap Map<String, RequestBody> params,
                                          @Part MultipartBody.Part file);

    @Multipart
    @POST("parcels")
    Call<PostParcelsResponse> postParcels(@HeaderMap Map<String, String> headers,
                                          @PartMap Map<String, RequestBody> params);


    @GET("parcels/download/{parcel-id}")
    Call<GetDownloadParcelResponse> getDownloadParcel(@HeaderMap Map<String, String> headers,
                                                      @Path("parcel-id") String parcelId);
}
