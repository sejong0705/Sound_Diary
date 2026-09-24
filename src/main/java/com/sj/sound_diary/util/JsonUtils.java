package com.sj.sound_diary.util;

import java.util.Map;

public class JsonUtils {

    private JsonUtils() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T field(Map<String, Object> json, String key) {
        return (T) json.get(key);
    }
}
