package com.pentasecurity.core;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class StringUtilsTest {
    @Test
    public void convertByteToStringTest() {
        String msg = "Hello, world";
        byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
        System.out.println(new String(msgBytes));
    }
}
