package com.pentasecurity.core;

import com.pentasecurity.core.utils.CryptoUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public class CryptoUtilsTest {
    @Test
    public void sha256Test() throws NoSuchAlgorithmException {
        String msg = "a";
        System.out.print(CryptoUtils.sha256(msg));
    }

    @Test
    public void bytesToHexTest() {
        String msg = "a";
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        System.out.print(CryptoUtils.bytesToHex(bytes));
    }
}
