package com.pentasecurity.core.service;

import com.pentasecurity.core.dto.market.*;
import com.pentasecurity.core.helper.RetrofitInitializer;

import lombok.extern.slf4j.Slf4j;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;
import java.util.List;

@Slf4j
public class MarketCommunicator {
    public static Retrofit retrofit = RetrofitInitializer.getRetrofitMarket();
    public static MarketHttpRequestor httpRequestor = retrofit.create(MarketHttpRequestor.class);

    public static String requestLogin(String userId, String password) {
        LoginResponse result = null;
        try {
            Response<LoginResponse> response = httpRequestor.login(new LoginRequest(userId, password)).execute();
            result = response.body();
        } catch (IOException e) {
            log.error("request login error happened: {}", e.getMessage());
        }
        return result.getData().getAccessToken();
    }

    public static void requestSaveParcel(String accessToken, String parcelId, long productId, String price) {
        String authorization = "Bearer " + accessToken;
        try {
            httpRequestor.postParcels(productId, authorization, new SaveParcelRequest(parcelId,productId, price))
                    .execute();
        } catch (IOException e) {
            log.error("request save parcel error happened: {}", e.getMessage());
        }
    }

    public static List<BuyerAutoOrderData> requestGetBuyerAutoOrders(String authorization, long buyerId) {
        BuyerAutoOrderResponse result = null;
        try {
            Response<BuyerAutoOrderResponse> response = httpRequestor.getBuyerAutoOrders(authorization, buyerId).execute();
            result = response.body();
        } catch (IOException e) {
            log.error("request buyer's auto orders error happened: {}", e.getMessage());
        }

        return result.getData();
    }

    public static List<AutoOrderFileData> requestGetAutoOrderFiles(String authorization, long orderId) {
        AutoOrderFileResponse result = null;
        try {
            Response<AutoOrderFileResponse> response = httpRequestor.getAutoOrderFiles(authorization, orderId).execute();
            result = response.body();
        } catch (IOException e) {
            log.error("request auto order's files error happened: {}", e.getMessage());
        }

        return result.getData();
    }

    public static void requestPostOrderFile(String authorization,
                                            long buyerId,
                                            long sellerId,
                                            long productId,
                                            long fileId) {
        try {
            httpRequestor.postOrderFile(authorization,
                    new OrderFileRequest(buyerId, sellerId, productId, fileId, "AUTO")).execute();
        } catch (IOException e) {
            log.error("request order file error happened: {}", e.getMessage());
        }
    }

    public static List<SellerAutoOrderData> requestGetSellerAutoOrders(String authorization, long sellerId) {
        SellerAutoOrderResponse result = null;
        try {
            Response<SellerAutoOrderResponse> response = httpRequestor.getSellerAutoOrders(authorization, sellerId).execute();
            result = response.body();
        } catch (IOException e) {
            log.error("request seller's auto orders error happened: {}", e.getMessage());
        }

        return result.getData();
    }

    public static void requestPatchOrder(String authorization, long orderId) {
        try {
            httpRequestor.patchOrder(authorization, orderId,
                    new PatchOrderRequest(orderId, "ORDER_GRANT")).execute();
        } catch (IOException e) {
            log.error("request patch order error happened: {}", e.getMessage());
        }
    }
}
