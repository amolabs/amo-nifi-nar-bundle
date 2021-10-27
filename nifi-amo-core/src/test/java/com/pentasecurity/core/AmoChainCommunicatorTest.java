package com.pentasecurity.core;

import com.google.gson.Gson;
import com.pentasecurity.core.dto.chain.StorageInfo;
import com.pentasecurity.core.dto.chain.StorageResponse;
import com.pentasecurity.core.service.AmoChainCommunicator;
import com.pentasecurity.core.utils.CryptoUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Base64;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class AmoChainCommunicatorTest {
    @Test
    public void getLatestBlockHeightTest() throws InterruptedException {
        String latestBlockHeight = AmoChainCommunicator.getLatestBlockHeight();
        System.out.println(latestBlockHeight);
    }

    @Test
    public void getStorageTest() throws IOException {
        String storageIdHex = CryptoUtils.bytesToHex("12".getBytes(StandardCharsets.UTF_8));
        System.out.println(storageIdHex);
        StorageResponse response = AmoChainCommunicator.getStorage(storageIdHex);

        int code = response.getStorageResult().getResponse().getCode();
        String value = response.getStorageResult().getResponse().getValue();

        System.out.println(code);
        System.out.println(value);

        if (StringUtils.isNotEmpty(value)) {
            StorageInfo storageInfo = new Gson().fromJson(new String(Base64.decode(value)), StorageInfo.class);

            System.out.println(storageInfo.getOwner());
        }
    }

    @Test
    public void requestRegisterTx() {
        // TODO implement
    }
}
