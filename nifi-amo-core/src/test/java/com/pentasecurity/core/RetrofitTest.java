package com.pentasecurity.core;

import com.pentasecurity.core.dto.storage.Operation;
import com.pentasecurity.core.dto.storage.PostAuthRequest;
import com.pentasecurity.core.dto.storage.PostAuthResponse;
import com.pentasecurity.core.helper.RetrofitInitializer;
import com.pentasecurity.core.service.AmoStorageHttpRequestor;
import org.junit.Test;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;

public class RetrofitTest {
    @Test
    public void retrofitTest() throws InterruptedException {
        Retrofit retrofit = RetrofitInitializer.getRetrofitAmoStorage();
        AmoStorageHttpRequestor httpRequestor = retrofit.create(AmoStorageHttpRequestor.class);
        Operation operation = new Operation("upload");
        operation.setHash("aA12f");
        try {
            Response<PostAuthResponse> response = httpRequestor.postAuth(
                    new PostAuthRequest("kevin", operation)
            ).execute();

            PostAuthResponse result = response.body();

            System.out.println("Success");
            System.out.println(result.getToken());
        } catch (IOException e) {
            System.out.println("Failed");
        }
    }
}
