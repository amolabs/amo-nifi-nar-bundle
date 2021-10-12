package com.pentasecurity.core;

import com.pentasecurity.core.dto.market.JwtLoginPayload;
import com.pentasecurity.core.dto.storage.Metadata;
import com.pentasecurity.core.dto.rpc.JsonRpc;
import com.pentasecurity.core.utils.JsonUtils;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class JsonUtilsTest {
    @Test
    public void toJsonTest() {
        String owner = "aaaaaaaaaaaaaa";
        String hash = "bbbbbbbbbbbbbbbbbbbbb";
        Metadata metadata = new Metadata(owner, hash);
        System.out.println(JsonUtils.toJson(metadata));

        assertThat(JsonUtils.toJson(metadata), is("{\"owner\":\"aaaaaaaaaaaaaa\",\"hash\":\"bbbbbbbbbbbbbbbbbbbbb\"}"));
    }

    @Test
    public void toJsonTest2() {
        Object params = null;
        String jsonrpc = "2.0";
        String id = "status";
        String method = "status";

        JsonRpc jsonRpc = new JsonRpc(jsonrpc, id, method);
        System.out.println(JsonUtils.toJson(jsonRpc));

        assertThat(JsonUtils.toJson(jsonRpc), is("{\"jsonrpc\":\"2.0\",\"id\":\"status\",\"method\":\"status\"}"));
    }

    @Test
    public void fromJsonTest() {
        String json = "{\"sub\":\"hjs6877@gmail.com\",\"role\":[\"ROLE_USER\"],\"sellerId\":33,\"buyerId\":30,\"exp\":1632451265,\"iat\":1632447665,\"memberId\":45}";
        JwtLoginPayload loginPayload = (JwtLoginPayload) JsonUtils.fromJson(json, JwtLoginPayload.class);

        assertThat(loginPayload.getSellerId(), is(33L));
        assertThat(loginPayload.getBuyerId(), is(30L));
        assertThat(loginPayload.getMemberId(), is(45L));
    }
}
