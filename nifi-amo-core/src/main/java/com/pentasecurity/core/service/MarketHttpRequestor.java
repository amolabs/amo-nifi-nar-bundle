package com.pentasecurity.core.service;

import com.pentasecurity.core.dto.market.*;
import retrofit2.Call;
import retrofit2.http.*;

public interface MarketHttpRequestor {
    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest body);

    @POST("products/{product-id}/files")
    Call<ParcelResponse> postParcels(@Path("product-id") long productId, @Header("Authorization") String token,
                           @Body SaveParcelRequest body);

    @GET("buyers/{buyer-id}/auto-orders")
    Call<BuyerAutoOrderResponse> getBuyerAutoOrders(@Header("Authorization") String token,
                                                    @Path("buyer-id") long buyerId);

    @GET("auto-orders/{auto-order-id}/files")
    Call<AutoOrderFileResponse> getAutoOrderFiles(@Header("Authorization") String token,
                                                  @Path("auto-order-id") long autoOrderId);

    @POST("orders")
    Call<Void> postOrderFile(@Header("Authorization") String token, @Body OrderFileRequest body);

    @GET("sellers/{seller-id}/auto-orders")
    Call<SellerAutoOrderResponse> getSellerAutoOrders(@Header("Authorization") String token,
                                                      @Path("seller-id") long sellerId);

    @GET("sellers/{seller-id}/orders/auto-orders")
    Call<SellerOrderResponse> getSellerOrders(@Header("Authorization") String token,
                                                      @Path("seller-id") long sellerId);

    @PATCH("orders/{order-id}")
    Call<Void> patchOrder(@Header("Authorization") String token,
                          @Path("order-id") long orderId, @Body PatchOrderRequest body);
}
