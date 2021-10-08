package com.pentasecurity.core;

import com.pentasecurity.core.dto.Metadata;
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
}
