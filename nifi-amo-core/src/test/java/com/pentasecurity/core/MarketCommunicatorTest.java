package com.pentasecurity.core;

import com.pentasecurity.core.service.MarketCommunicator;
import org.junit.Test;

public class MarketCommunicatorTest {
    @Test
    public void loginTest() {
        String userId = "hjs6877@gmail.com";
        String password = "Jshwang1234!";

        String accessToken = MarketCommunicator.requestLogin(userId, password);

        System.out.println(accessToken);
    }
}
