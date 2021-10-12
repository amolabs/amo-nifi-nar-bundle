package com.pentasecurity.core.utils;

import com.google.gson.Gson;

public class JsonUtils {
    private static Gson gson = new Gson();
    public static String toJson(Object object) {
        return gson.toJson(object);
    }
    public static Object fromJson(String json, Class clazz) {
        return gson.fromJson(json, clazz);
    }
}
