package com.sj.sound_diary.util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

public class RestTemplateUtils {

    private RestTemplateUtils() {
    }

    public static <T> T get(RestTemplate restTemplate, String url, HttpEntity<?> entity, Class<T> responseType) {
        return restTemplate.exchange(url, HttpMethod.GET, entity, responseType).getBody();
    }

    public static <T> T post(RestTemplate restTemplate, String url, HttpEntity<?> entity, Class<T> responseType) {
        return restTemplate.postForObject(url, entity, responseType);
    }
}
