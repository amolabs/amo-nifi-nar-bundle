package com.pentasecurity.core.service;

import com.google.gson.Gson;
import com.pentasecurity.core.dto.market.*;
import com.pentasecurity.core.helper.RetrofitInitializer;

import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
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
            throw new RuntimeException(e);
        }
        return result.getData().getAccessToken();
    }

    public static void requestSaveParcel(String accessToken, String parcelId, long productId, String price) {
        String authorization = "Bearer " + accessToken;
        try {
            Response<ParcelResponse> response = httpRequestor.postParcels(productId, authorization, new SaveParcelRequest(parcelId,productId, price))
                    .execute();
            ParcelResponse result = response.body();
            ResponseBody error = response.errorBody();

            if (error != null) {
                Gson gson = new Gson();
                ParcelErrorResponse errorResult = gson.fromJson(error.string(), ParcelErrorResponse.class);
                if (errorResult != null && !errorResult.isSuccess()) {
                    throw new Exception(errorResult.getMessage());
                }
            }

            log.info("# response file ID: {}", result.getData().getFileId());
        } catch (Exception e) {
            log.error("request save parcel error happened: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static List<BuyerAutoOrderData> requestGetBuyerAutoOrders(String authorization, long buyerId) {
        BuyerAutoOrderResponse result = null;
        try {
            Response<BuyerAutoOrderResponse> response = httpRequestor.getBuyerAutoOrders(authorization, buyerId).execute();

            result = response.body();
        } catch (IOException e) {
            log.error("request buyer's auto orders error happened: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return result.getData();
    }

    public static List<OrderData> requestGetBuyerOrders(String authorization, long buyerId) {
        OrderResponse result = null;
        try {
            Response<OrderResponse> response = httpRequestor.getBuyerOrders(authorization, buyerId).execute();
            result = response.body();
        } catch (IOException e) {
            log.error("request buyer's orders error happened: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return result.getData();
    }

    public static List<AutoOrderFileData> requestGetAutoOrderFiles(String authorization, long autoOrderId) {
        AutoOrderFileResponse result = null;
        try {
            Response<AutoOrderFileResponse> response = httpRequestor.getAutoOrderFiles(authorization, autoOrderId).execute();
            result = response.body();
        } catch (IOException e) {
            log.error("request auto order's files error happened: {}", e.getMessage());
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
    }

    public static List<SellerAutoOrderData> requestGetSellerAutoOrders(String authorization, long sellerId) {
        SellerAutoOrderResponse result = null;
        try {
            Response<SellerAutoOrderResponse> response = httpRequestor.getSellerAutoOrders(authorization, sellerId).execute();
            result = response.body();
        } catch (IOException e) {
            log.error("request seller's auto orders error happened: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return result.getData();
    }

    public static List<OrderData> requestGetSellerOrders(String authorization, long sellerId) {
        OrderResponse result = null;
        try {
            Response<OrderResponse> response = httpRequestor.getSellerOrders(authorization, sellerId).execute();
            result = response.body();
        } catch (IOException e) {
            log.error("request seller's orders error happened: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return result.getData();
    }

    /**
     * TODO 업데이트 할 정보를 넘겨주는 걸로 수정 필요.(업데이트 할 정보를 동적으로 받아야 한다. 지금은 ORDER_GRANT가 고정으로 박혀 있음)
     * isDownloaded도 넘겨 줄 수 있어야 됨.
     * @param authorization
     * @param orderId
     */

    public static void requestPatchOrder(String authorization, long orderId) {
        try {
            httpRequestor.patchOrder(authorization, orderId,
                    new PatchOrderRequest(orderId, "ORDER_GRANT")).execute();
        } catch (IOException e) {
            log.error("request patch order error happened: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
