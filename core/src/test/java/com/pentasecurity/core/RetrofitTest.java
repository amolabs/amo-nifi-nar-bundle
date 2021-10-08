package com.pentasecurity.core;

import com.pentasecurity.core.dto.Operation;
import com.pentasecurity.core.dto.PostAuthRequest;
import com.pentasecurity.core.dto.PostAuthResponse;
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

        try {
            Response<PostAuthResponse> response = httpRequestor.postAuth(
                    new PostAuthRequest("kevin", new Operation("upload", "aA12f"))
            ).execute();

            PostAuthResponse result = response.body();

            System.out.println("Success");
            System.out.println(result.getToken());
        } catch (IOException e) {
            System.out.println("Failed");
        }
    }
}
