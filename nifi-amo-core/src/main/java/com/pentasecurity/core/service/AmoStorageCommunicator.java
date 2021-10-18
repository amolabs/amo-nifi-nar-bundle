package com.pentasecurity.core.service;

import com.pentasecurity.core.dto.storage.*;
import com.pentasecurity.core.helper.RetrofitInitializer;
import com.pentasecurity.core.utils.CryptoUtils;
import com.pentasecurity.core.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AmoStorageCommunicator {
    public static Retrofit retrofit = RetrofitInitializer.getRetrofitAmoStorage();
    public static AmoStorageHttpRequestor httpRequestor = retrofit.create(AmoStorageHttpRequestor.class);

    public static String requestAuthToken(String owner, String hashContent) {
        PostAuthResponse result = null;
        try {
            Response<PostAuthResponse> response = httpRequestor.postAuth(
                    new PostAuthRequest(owner, new Operation("upload", hashContent))
            ).execute();

            result = response.body();

        } catch (IOException e) {
            log.error("request post auth error happened: {}", e.getMessage());
        }
        return result.getToken();
    }

    public static String requestUpload(String owner,
                                       String hash,
                                       String accessToken,
                                       byte[] publicKey,
                                       byte[] signature,
                                       byte[] content) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("X-Auth-Token", accessToken);
        headerMap.put("X-Public-Key", CryptoUtils.bytesToHex(publicKey));
        headerMap.put("X-Signature", CryptoUtils.bytesToHex(signature));

        Metadata metadata = new Metadata(owner, hash);
        RequestBody ownerBody = RequestBody.create(MediaType.parse("text/plain"), owner);
        RequestBody metadataBody =
                RequestBody.create(MediaType.parse("application/json"), JsonUtils.toJson(metadata));
        RequestBody fileBody = RequestBody.create(MediaType.parse("text/plain"), CryptoUtils.bytesToHex(content));

        Map<String, RequestBody> partMap = new HashMap<>();
        partMap.put("owner", ownerBody);
        partMap.put("metadata", metadataBody);
        partMap.put("file", fileBody);

        // TODO Multipart 동작 안함. 나중에 해결
//        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), content);
//        MultipartBody.Part partFile = MultipartBody.Part.createFormData("file", null, fileBody);

        PostParcelsResponse result = null;
        try {
            Response<PostParcelsResponse> response =
                    httpRequestor.postParcels(headerMap, partMap).execute();

            result = response.body();

        } catch (IOException e) {
            log.error("request post parcels error happened: {}", e.getMessage());
        }

        return result.getParcelId();
    }

    public static String requestDownload(String parcelId,
                                         String accessToken,
                                         byte[] publicKey,
                                         byte[] signature) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("X-Auth-Token", accessToken);
        headerMap.put("X-Public-Key", CryptoUtils.bytesToHex(publicKey));
        headerMap.put("X-Signature", CryptoUtils.bytesToHex(signature));

        GetDownloadParcelResponse result = null;
        try {
            Response<GetDownloadParcelResponse> response =
                    httpRequestor.getDownloadParcel(headerMap, parcelId).execute();

            result = response.body();

        } catch (IOException e) {
            log.error("request get download parcel error happened: {}", e.getMessage());
        }

        return result.getData();
    }
}
