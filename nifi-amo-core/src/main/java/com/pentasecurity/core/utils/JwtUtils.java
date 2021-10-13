package com.pentasecurity.core.utils;

import java.util.Base64;

public class JwtUtils {
    public static String decodeHeader(String accessToken) {
        String[] chunks = accessToken.split("\\.");
        Base64.Decoder decoder = Base64.getDecoder();

        String header = new String(decoder.decode(chunks[0]));

        return header;
    }

    public static String decodePayload(String accessToken) {
        String[] chunks = accessToken.split("\\.");
        Base64.Decoder decoder = Base64.getDecoder();

        String payload = new String(decoder.decode(chunks[1]));

        return payload;
    }
}
